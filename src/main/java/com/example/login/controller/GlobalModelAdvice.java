package com.example.login.controller;

import com.example.login.constant.SessionConst;
import com.example.login.domain.DoMember;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.SessionAttribute;

@ControllerAdvice
public class GlobalModelAdvice {

    /** 모든 요청의 모델에 loginMember를 자동으로 담아줌 (없으면 null) */
    @ModelAttribute("loginMember")
    public DoMember loginMember(
            @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) DoMember loginMember) {
        return loginMember;
    }
}