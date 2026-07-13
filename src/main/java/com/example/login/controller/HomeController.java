package com.example.login.controller;

import com.example.login.domain.DoMember;
import com.example.login.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final MemberService memberService;

    @GetMapping("/")
    public String home(@SessionAttribute(name=SessionConst.LOGIN_MEMBER, required = false)  DoMember loginMember, Model model){

        //세션에 회원정보가 없음
        if(loginMember == null) {
            return "home";
        }

        model.addAttribute("loginMember", loginMember);
        return "home";
    }
}
