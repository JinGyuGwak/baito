package com.baito.my_app.member.application.service;

import com.baito.my_app.member.application.port.in.MemberProfileUseCase;
import com.baito.my_app.member.application.port.out.MemberRepository;
import com.baito.my_app.member.domain.Member;
import com.baito.my_app.member.domain.MemberNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MemberProfileService implements MemberProfileUseCase {

    private final MemberRepository memberRepository;

    public MemberProfileService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Member getMyProfile(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("회원을 찾을 수 없습니다."));
    }

    @Override
    public Member changeName(Long memberId, String name) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("회원을 찾을 수 없습니다."));
        member.changeName(name);
        return memberRepository.save(member);
    }
}
