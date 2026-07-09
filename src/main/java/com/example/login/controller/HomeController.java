package com.example.login.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
public class HomeController {

    // loginMember는 GlobalModelAdvice가 모델에 넣어주므로 여기선 처리 불필요
    @GetMapping("/")
    public String homeLogin() {
        return "home";
    }
}