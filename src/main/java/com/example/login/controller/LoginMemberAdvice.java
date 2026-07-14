package com.example.login.controller;

import com.example.login.domain.DoMember;
import com.example.login.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class LoginMemberAdvice {

    private final MemberService memberService;

    @ModelAttribute("loginMember")
    public DoMember loginMember(Authentication authentication) {

        // 로그인 안 한 경우 (인증 없음 or 익명 토큰)
        if (authentication == null ||
                authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }

        // 시큐리티가 보관한 로그인 아이디 꺼내기
        String loginId = authentication.getName();

        // DB 조회 후 모든 뷰에 loginMember로 공급
        return memberService.findByLoginId(loginId)
                .orElse(null);
    }
}