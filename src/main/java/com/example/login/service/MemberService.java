package com.example.login.service;

import com.example.login.domain.DoMember;
import com.example.login.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

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

    //회원 삭제
    public void delete(Long memberId){
        memberRepository.deleteById(memberId);
    }



}
