package com.example.login.service;

import com.example.login.domain.DoMember;
import com.example.login.domain.UserRoll;
import com.example.login.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoginService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // 사용자 조회 (없으면 시큐리티 표준 예외)
        Optional<DoMember> findLoginId = memberRepository.findByLoginId(username);

        if (findLoginId.isEmpty()) {
            log.info("사용자를 찾을 수 없음: {}", username);
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다");
        }
        DoMember findMember = findLoginId.get();
        log.info("로그인 사용자 아이디 {}", findMember.getLoginId());

        // 권한 부여: grade 문자열 → ROLE_ 권한 객체
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        if ("user".equals(findMember.getGrade())) {
            grantedAuthorities.add(new SimpleGrantedAuthority(UserRoll.USER.getValue()));
        } else {
            grantedAuthorities.add(new SimpleGrantedAuthority(UserRoll.ADMIN.getValue()));
        }
        log.info("찍어봐? {}, {}, {}", findMember.getLoginId(), findMember.getPassword(), grantedAuthorities);

        // 시큐리티에게 넘길 인증 정보 (loginId, 암호화된 비번, 권한 목록)
        return new User(findMember.getLoginId(), findMember.getPassword(), grantedAuthorities);
        // ↑ org.springframework.security.core.userdetails.User (시큐리티 제공 클래스)
    }
}