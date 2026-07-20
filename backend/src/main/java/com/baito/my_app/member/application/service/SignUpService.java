package com.baito.my_app.member.application.service;

import com.baito.my_app.member.application.port.in.SignUpUseCase;
import com.baito.my_app.member.application.port.out.MemberRepository;
import com.baito.my_app.member.application.port.out.PasswordEncryptor;
import com.baito.my_app.member.domain.DuplicateLoginIdException;
import com.baito.my_app.member.domain.Member;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SignUpService implements SignUpUseCase {

    private final MemberRepository memberRepository;
    private final PasswordEncryptor passwordEncryptor;

    public SignUpService(MemberRepository memberRepository, PasswordEncryptor passwordEncryptor) {
        this.memberRepository = memberRepository;
        this.passwordEncryptor = passwordEncryptor;
    }

    @Override
    public Long signUp(Command command) {
        if (memberRepository.existsByLoginId(command.loginId())) {
            throw new DuplicateLoginIdException(command.loginId());
        }
        Member member = Member.register(
                command.loginId(),
                passwordEncryptor.encode(command.rawPassword()),
                command.name(),
                command.role()
        );
        return memberRepository.save(member).id();
    }
}
