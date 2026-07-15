package com.example.login.controller;

import com.example.login.domain.DoMember;
import com.example.login.service.GoogleService;
import com.example.login.service.LoginService;
import com.example.login.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class GoogleLoginController {

    @Value("${google.client-id}")
    private String clientId;

    @Value("${google.redirect-uri}")
    private String redirectUri;

    private final GoogleService googleService;
    private final MemberService memberService;
    private final LoginService loginService;

    // 버튼 클릭 → 구글 인증 페이지로 보내기
    @GetMapping("/oauth/google")
    public String googleLogin() {
        String googleAuthUrl = "https://accounts.google.com/o/oauth2/v2/auth"
                + "?response_type=code"
                + "&client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&scope=email%20profile";   // 구글은 scope 필수 (%20 = 공백)

        return "redirect:" + googleAuthUrl;
    }

    // 구글이 code를 들고 돌아오는 자리 → 회원 연동 + Security 로그인 처리
    @GetMapping("/oauth/google/callback")
    public String googleCallback(@RequestParam String code, HttpServletRequest request) {

        // 1. 인가코드 → access_token 교환 (구글은 client_secret 포함)
        String accessToken = googleService.getAccessToken(code);

        // 2. access_token → 구글 사용자 정보 조회
        Map<String, Object> userInfo = googleService.getUserInfo(accessToken);
        log.info("구글 사용자 정보 = {}", userInfo);

        // 3. id/이름 추출 — 구글은 평평한 구조 + id가 이미 String (카카오와 차이)
        String googleId = String.valueOf(userInfo.get("id"));
        String name = String.valueOf(userInfo.get("name"));

        // 4. 우리 사이트 전용 loginId 생성
        String loginId = "google_" + googleId;

        // 5. 회원 조회 → 없으면 자동 가입 (최초 1회)
        DoMember member = memberService.findByLoginId(loginId)
                .orElseGet(() -> memberService.joinGoogleMember(loginId, name));

        // 6. Spring Security 수동 로그인 처리 (카카오와 완전 동일한 6단계)
        UserDetails userDetails = loginService.loadUserByUsername(member.getLoginId());

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        // ★ 세션 저장 — 없으면 redirect 후 로그인 풀림
        request.getSession().setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                context);

        log.info("구글 로그인 완료: {}", loginId);
        return "redirect:/";
    }
}