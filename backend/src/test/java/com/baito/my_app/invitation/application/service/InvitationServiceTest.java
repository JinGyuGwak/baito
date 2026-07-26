package com.baito.my_app.invitation.application.service;

import com.baito.my_app.group.application.port.out.WorkGroupRepository;
import com.baito.my_app.group.domain.NotGroupOwnerException;
import com.baito.my_app.group.domain.WorkGroup;
import com.baito.my_app.invitation.application.port.in.CancelInvitationUseCase;
import com.baito.my_app.invitation.application.port.in.InviteMemberUseCase;
import com.baito.my_app.invitation.application.port.in.RespondInvitationUseCase;
import com.baito.my_app.invitation.application.port.out.InvitationRepository;
import com.baito.my_app.invitation.domain.DuplicatePendingInvitationException;
import com.baito.my_app.invitation.domain.Invitation;
import com.baito.my_app.invitation.domain.InvitationAccessDeniedException;
import com.baito.my_app.invitation.domain.InvitationNotFoundException;
import com.baito.my_app.invitation.domain.InvitationStatus;
import com.baito.my_app.invitation.domain.InvalidInviteeException;
import com.baito.my_app.member.application.port.out.MemberRepository;
import com.baito.my_app.member.domain.Member;
import com.baito.my_app.member.domain.Role;
import com.baito.my_app.membership.application.port.out.GroupMembershipRepository;
import com.baito.my_app.membership.domain.GroupMembership;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InvitationServiceTest {

    private static final Long GROUP_ID = 10L;
    private static final Long OWNER_ID = 1L;
    private static final Long INVITEE_ID = 2L;

    @Mock
    private InvitationRepository invitationRepository;
    @Mock
    private WorkGroupRepository workGroupRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private GroupMembershipRepository membershipRepository;
    @InjectMocks
    private InvitationService service;

    private WorkGroup ownedGroup() {
        return new WorkGroup(GROUP_ID, OWNER_ID, "강남점", null, null);
    }

    private Member partTimer() {
        return new Member(INVITEE_ID, "worker01", "pw", "알바", Role.PART_TIMER, null);
    }

    @Nested
    @DisplayName("invite")
    class Invite {

        private InviteMemberUseCase.Command command() {
            return new InviteMemberUseCase.Command(GROUP_ID, OWNER_ID, "worker01");
        }

        @Test
        @DisplayName("성공 - PENDING 초대를 저장하고 생성된 id를 반환한다")
        void invite() {
            given(workGroupRepository.findById(GROUP_ID)).willReturn(Optional.of(ownedGroup()));
            given(memberRepository.findByLoginId("worker01")).willReturn(Optional.of(partTimer()));
            given(membershipRepository.existsActiveMembership(GROUP_ID, INVITEE_ID)).willReturn(false);
            given(invitationRepository.existsPendingByGroupIdAndInviteeId(GROUP_ID, INVITEE_ID)).willReturn(false);
            given(invitationRepository.save(any())).willAnswer(inv -> {
                Invitation i = inv.getArgument(0);
                i.setId(50L);
                return i;
            });

            Long id = service.invite(command());

            assertThat(id).isEqualTo(50L);
        }

        @Test
        @DisplayName("실패 - 그룹이 없으면 NotGroupOwnerException")
        void groupNotFound() {
            given(workGroupRepository.findById(GROUP_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> service.invite(command()))
                    .isInstanceOf(NotGroupOwnerException.class);
            verify(invitationRepository, never()).save(any());
        }

        @Test
        @DisplayName("실패 - 요청자가 그룹 소유자가 아니면 NotGroupOwnerException")
        void notOwner() {
            given(workGroupRepository.findById(GROUP_ID))
                    .willReturn(Optional.of(new WorkGroup(GROUP_ID, 999L, "강남점", null, null)));

            assertThatThrownBy(() -> service.invite(command()))
                    .isInstanceOf(NotGroupOwnerException.class);
        }

        @Test
        @DisplayName("실패 - 초대 대상 로그인 ID가 없으면 InvalidInviteeException")
        void inviteeNotFound() {
            given(workGroupRepository.findById(GROUP_ID)).willReturn(Optional.of(ownedGroup()));
            given(memberRepository.findByLoginId("worker01")).willReturn(Optional.empty());

            assertThatThrownBy(() -> service.invite(command()))
                    .isInstanceOf(InvalidInviteeException.class);
        }

        @Test
        @DisplayName("실패 - 초대 대상이 알바가 아니면 InvalidInviteeException")
        void inviteeNotPartTimer() {
            given(workGroupRepository.findById(GROUP_ID)).willReturn(Optional.of(ownedGroup()));
            given(memberRepository.findByLoginId("worker01"))
                    .willReturn(Optional.of(new Member(INVITEE_ID, "worker01", "pw", "사장님", Role.OWNER, null)));

            assertThatThrownBy(() -> service.invite(command()))
                    .isInstanceOf(InvalidInviteeException.class);
        }

        @Test
        @DisplayName("실패 - 이미 활성 멤버면 InvalidInviteeException")
        void alreadyMember() {
            given(workGroupRepository.findById(GROUP_ID)).willReturn(Optional.of(ownedGroup()));
            given(memberRepository.findByLoginId("worker01")).willReturn(Optional.of(partTimer()));
            given(membershipRepository.existsActiveMembership(GROUP_ID, INVITEE_ID)).willReturn(true);

            assertThatThrownBy(() -> service.invite(command()))
                    .isInstanceOf(InvalidInviteeException.class);
        }

        @Test
        @DisplayName("실패 - 이미 PENDING 초대가 있으면 DuplicatePendingInvitationException")
        void duplicatePending() {
            given(workGroupRepository.findById(GROUP_ID)).willReturn(Optional.of(ownedGroup()));
            given(memberRepository.findByLoginId("worker01")).willReturn(Optional.of(partTimer()));
            given(membershipRepository.existsActiveMembership(GROUP_ID, INVITEE_ID)).willReturn(false);
            given(invitationRepository.existsPendingByGroupIdAndInviteeId(GROUP_ID, INVITEE_ID)).willReturn(true);

            assertThatThrownBy(() -> service.invite(command()))
                    .isInstanceOf(DuplicatePendingInvitationException.class);
        }
    }

    @Nested
    @DisplayName("cancel")
    class Cancel {

        @Test
        @DisplayName("성공 - 본인이 보낸 초대를 CANCELLED로 만든다")
        void cancel() {
            Invitation invitation = new Invitation(50L, GROUP_ID, OWNER_ID, INVITEE_ID,
                    InvitationStatus.PENDING, LocalDateTime.now(), null);
            given(invitationRepository.findById(50L)).willReturn(Optional.of(invitation));

            service.cancel(new CancelInvitationUseCase.Command(50L, OWNER_ID));

            assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.CANCELLED);
            verify(invitationRepository).save(invitation);
        }

        @Test
        @DisplayName("실패 - 초대가 없으면 InvitationNotFoundException")
        void notFound() {
            given(invitationRepository.findById(50L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> service.cancel(new CancelInvitationUseCase.Command(50L, OWNER_ID)))
                    .isInstanceOf(InvitationNotFoundException.class);
        }

        @Test
        @DisplayName("실패 - 초대를 보낸 사장이 아니면 InvitationAccessDeniedException")
        void notInviter() {
            Invitation invitation = new Invitation(50L, GROUP_ID, OWNER_ID, INVITEE_ID,
                    InvitationStatus.PENDING, LocalDateTime.now(), null);
            given(invitationRepository.findById(50L)).willReturn(Optional.of(invitation));

            assertThatThrownBy(() -> service.cancel(new CancelInvitationUseCase.Command(50L, 999L)))
                    .isInstanceOf(InvitationAccessDeniedException.class);
            verify(invitationRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("accept / reject")
    class Respond {

        private Invitation pending() {
            return new Invitation(50L, GROUP_ID, OWNER_ID, INVITEE_ID,
                    InvitationStatus.PENDING, LocalDateTime.now(), null);
        }

        @Test
        @DisplayName("accept 성공 - 초대를 ACCEPTED로 만들고 멤버십을 활성화한다")
        void accept() {
            Invitation invitation = pending();
            given(invitationRepository.findById(50L)).willReturn(Optional.of(invitation));
            given(membershipRepository.findByGroupIdAndMemberId(GROUP_ID, INVITEE_ID)).willReturn(Optional.empty());

            service.accept(new RespondInvitationUseCase.Command(50L, INVITEE_ID));

            assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.ACCEPTED);
            verify(invitationRepository).save(invitation);
            verify(membershipRepository).save(any(GroupMembership.class));
        }

        @Test
        @DisplayName("accept 멱등 - 이미 멤버십이 있으면 새로 저장하지 않는다")
        void accept_idempotentMembership() {
            Invitation invitation = pending();
            given(invitationRepository.findById(50L)).willReturn(Optional.of(invitation));
            given(membershipRepository.findByGroupIdAndMemberId(GROUP_ID, INVITEE_ID))
                    .willReturn(Optional.of(GroupMembership.activate(GROUP_ID, INVITEE_ID, LocalDateTime.now())));

            service.accept(new RespondInvitationUseCase.Command(50L, INVITEE_ID));

            verify(membershipRepository, never()).save(any());
        }

        @Test
        @DisplayName("accept 실패 - 초대 수신자가 아니면 InvitationAccessDeniedException")
        void accept_notInvitee() {
            given(invitationRepository.findById(50L)).willReturn(Optional.of(pending()));

            assertThatThrownBy(() -> service.accept(new RespondInvitationUseCase.Command(50L, 999L)))
                    .isInstanceOf(InvitationAccessDeniedException.class);
            verify(membershipRepository, never()).save(any());
        }

        @Test
        @DisplayName("reject 성공 - 초대를 REJECTED로 만들고 멤버십은 만들지 않는다")
        void reject() {
            Invitation invitation = pending();
            given(invitationRepository.findById(50L)).willReturn(Optional.of(invitation));

            service.reject(new RespondInvitationUseCase.Command(50L, INVITEE_ID));

            assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.REJECTED);
            verify(invitationRepository).save(invitation);
            verify(membershipRepository, never()).save(any());
        }
    }
}
