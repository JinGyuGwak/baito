package com.baito.my_app.invitation.application.port.out;

import com.baito.my_app.common.domain.PageResult;
import com.baito.my_app.invitation.domain.Invitation;
import com.baito.my_app.invitation.domain.InvitationStatus;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port for invitation persistence.
 */
public interface InvitationRepository {

    Invitation save(Invitation invitation);

    Optional<Invitation> findById(Long id);

    boolean existsPendingByGroupIdAndInviteeId(Long groupId, Long inviteeId);

    /**
     * Invitations an owner has sent for one group, newest first, paged.
     *
     * @param status optional status filter; {@code null} matches every status
     */
    PageResult<Invitation> findByInviterIdAndGroupId(Long inviterId, Long groupId, InvitationStatus status,
                                                     int page, int size);

    /** Pending invitations addressed to a part-timer, newest first. */
    List<Invitation> findPendingByInviteeId(Long inviteeId);
}
