package com.example.login.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    @GetMapping("/")
    public String home(){
        //loginMember는 LoginMemberAdvice가 모든 뷰에 자동 공급
        return "home";
    }
}
