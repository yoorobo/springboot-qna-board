package com.example.login.controller;

import com.example.login.dto.LoginForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
@Slf4j
public class LoginController {

    //로그인 폼 화면만 담당. 실제 인증 처리(POST /login)와 로그아웃(POST /logout)은
    //스프링 시큐리티 필터가 수행한다. (SecurityConfig 참고)
    @GetMapping("/login")
    public String loginForm(Model model){
        model.addAttribute("loginForm", new LoginForm());
        return "user/loginForm";
    }
}
