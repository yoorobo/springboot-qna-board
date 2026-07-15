package com.example.login.controller;

import com.example.login.domain.DoMember;
import com.example.login.service.KakaoService;
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
public class KakaoLoginController {

    @Value("${kakao.client-id}")        // application.yaml의 kakao.client-id 주입
    private String clientId;

    @Value("${kakao.redirect-uri}")     // application.yaml의 kakao.redirect-uri 주입
    private String redirectUri;

    private final KakaoService kakaoService;
    private final MemberService memberService;
    private final LoginService loginService;

    // 버튼 클릭 → 카카오 인증 페이지로 보내기
    // 공식문서: kauth.kakao.com/oauth/authorize?response_type=code&client_id=...&redirect_uri=...
    @GetMapping("/oauth/kakao")
    public String kakaoLogin() {
        String kakaoAuthUrl = "https://kauth.kakao.com/oauth/authorize"
                + "?response_type=code"
                + "&client_id=" + clientId
                + "&redirect_uri=" + redirectUri;
        return "redirect:" + kakaoAuthUrl;
    }

    // 카카오가 code를 들고 돌아오는 자리 → 회원 연동 + Security 로그인 처리
    @GetMapping("/oauth/kakao/callback")
    public String kakaoCallback(@RequestParam String code, HttpServletRequest request) {

        // 1. 인가코드 → access_token 교환 (서버 ↔ 카카오 직접 통신)
        String accessToken = kakaoService.getAccessToken(code);

        // 2. access_token → 카카오 사용자 정보 조회
        Map<String, Object> userInfo = kakaoService.getUserInfo(accessToken);
        log.info("사용자 정보 = {}", userInfo);

        // 3. 회원번호 추출 — JSON 숫자는 Integer/Long 불확실 → Number로 받아 안전 변환
        long kakaoId = ((Number) userInfo.get("id")).longValue();

        // 4. 닉네임 추출 — 공식 표준 경로: kakao_account → profile → nickname
        //    (properties.nickname 경로는 Deprecated라 미사용)
        Map<String, Object> kakaoAccount = (Map<String, Object>) userInfo.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        String nickname = String.valueOf(profile.get("nickname"));

        // 5. 우리 사이트 전용 loginId 생성 (일반 회원과 충돌 방지용 접두어)
        String loginId = "kakao_" + kakaoId;

        // 6. 회원 조회 → 있으면 그대로, 없으면 자동 회원가입 (최초 1회만 가입됨)
        DoMember member = memberService.findByLoginId(loginId)
                .orElseGet(() -> memberService.joinKakaoMember(loginId, nickname));

        // ================================================================
        // 7. Spring Security 수동 로그인 처리
        //    폼 로그인이라면 Security 필터가 자동으로 해주는 과정을,
        //    카카오 로그인은 필터를 안 타므로 우리가 직접 재현한다
        // ================================================================

        // 7-1. UserDetails 생성 (신분증 발급)
        //      LoginService가 DB에서 회원 조회 → 권한 생성 → Security 표준 규격으로 변환
        UserDetails userDetails = loginService.loadUserByUsername(member.getLoginId());

        // 7-2. Authentication 생성 (로그인 성공 도장)
        //      principal=사용자정보, credentials=null(비번 검증은 카카오가 이미 완료), authorities=권한
        //      3개 인자 생성자 = "인증 완료" 상태의 토큰 생성
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        // 7-3. 빈 SecurityContext 생성 (로그인 정보를 담을 보관함 준비)
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        // 7-4. 보관함에 로그인 성공 도장 넣기 → "이 사용자는 로그인 완료 상태"
        context.setAuthentication(authentication);

        // 7-5. 현재 요청에 적용 — 이 줄 이후부터 이번 요청에서 로그인 상태 활성화
        SecurityContextHolder.setContext(context);

        // 7-6. 세션에 저장 (★ 제일 중요)
        //      이 줄이 없으면 "현재 요청 한정" 로그인이라 redirect 되는 순간 풀린다
        //      세션에 저장해야 다음 요청부터 Security가 로그인 상태를 복원
        request.getSession().setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                context);

        log.info("카카오 로그인 완료: {}", loginId);
        return "redirect:/";
    }
}