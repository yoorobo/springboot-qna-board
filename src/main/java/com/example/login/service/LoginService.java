package com.example.login.service;

import com.example.login.domain.DoMember;
import com.example.login.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoginService {
    private final MemberRepository memberRepository;

    public DoMember login(String loginId, String password){
        Optional<DoMember> findMember = memberRepository.findByLoginId(loginId);
        if(findMember.isPresent()){
            if(findMember.get().getPassword().equals(password)){
                return findMember.get(); //로그인성공
            }
            else return null;  //비밀번호가 틀림
        }
        return null;  //아이디가 없음
    }
}
