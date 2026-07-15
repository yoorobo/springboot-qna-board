package com.example.login.service;

import com.example.login.domain.DoMember;
import com.example.login.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    //회원가입
    public Long join(DoMember member){
        validateDuplicateMember(member);
        memberRepository.save(member);
        return member.getId();
    }

    //회원업데이트
    public Long save(DoMember member){
        memberRepository.save(member);
        return member.getId();
    }

    //중복회원검증
    private void validateDuplicateMember(DoMember member){
        Optional<DoMember> findMember = memberRepository.findByLoginId(member.getLoginId());
        if(findMember.isPresent()){
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    //전체회원조회
    public List<DoMember> findMemberList(){
        return memberRepository.findAll();
    }

    //회원 한명 조회
    public Optional<DoMember> findOneMember(Long memberId){
        return memberRepository.findById(memberId);
    }

    //로그인 아이디로 회원 조회 (전역 loginMember 주입용)
    public Optional<DoMember> findByLoginId(String loginId){
        return memberRepository.findByLoginId(loginId);
    }

    //회원 삭제
    public void delete(Long memberId){
        memberRepository.deleteById(memberId);
    }

    //카카오 회원 저장
    public DoMember joinKakaoMember(String kakaoId, String nickname){
        DoMember doMember = new DoMember();

        doMember.setLoginId(kakaoId);
        doMember.setName(nickname);
        doMember.setPassword(passwordEncoder.encode("KAKAO_USER"));
        doMember.setGrade("user");

        return memberRepository.save(doMember);
    }

    //구글 회원 저장
    public DoMember joinGoogleMember(String googleId, String name){
        DoMember doMember = new DoMember();

        doMember.setLoginId(googleId);
        doMember.setName(name);
        doMember.setPassword(passwordEncoder.encode("GOOGLE_USER"));
        doMember.setGrade("user");

        return memberRepository.save(doMember);
    }
}