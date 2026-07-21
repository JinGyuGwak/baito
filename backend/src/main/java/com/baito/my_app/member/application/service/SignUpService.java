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
        if (memberRepository.existsByLoginId(command.getLoginId())) {
            throw new DuplicateLoginIdException(command.getLoginId());
        }
        Member member = Member.register(
                command.getLoginId(),
                passwordEncryptor.encode(command.getRawPassword()),
                command.getName(),
                command.getRole()
        );
        return memberRepository.save(member).getId();
    }
}
