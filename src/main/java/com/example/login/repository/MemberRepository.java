package com.example.login.repository;

import com.example.login.domain.DoMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<DoMember, Long>
{
    //사용자 아이디로 조회하는 기능 추가
    Optional<DoMember> findByLoginId(String loginId);
}
