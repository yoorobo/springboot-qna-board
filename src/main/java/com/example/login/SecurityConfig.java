package com.example.login;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    //인증 및 권한 필터 체인
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/images/**", "/js/**").permitAll()
                        .requestMatchers("/", "/add", "/error/**").permitAll()
                        .requestMatchers("/oauth/**").permitAll()   // ← 카카오 로그인 시작 + callback (비로그인 접근 필수)
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login").permitAll()      // ← 로그인 '페이지 주소'
                        .usernameParameter("loginId")         // ← 폼 필드 'name값'
                        .defaultSuccessUrl("/")               // ← 성공 시 이동
                        .failureUrl("/login?error"))          // ← 실패 시 이동 (에러 표시)
                .logout(logout -> logout
                        .logoutUrl("/logout")                 // ← 로그아웃 '요청 주소' (POST)
                        .logoutSuccessUrl("/")                // ← 로그아웃 후 홈으로
                        .invalidateHttpSession(true));        // ← 세션 무효화
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}