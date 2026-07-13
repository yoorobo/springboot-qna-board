package com.example.login.controller;

import com.example.login.domain.DoMember;
import com.example.login.dto.LoginForm;
import com.example.login.service.LoginService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
@Slf4j
public class LoginController {

    private final LoginService loginService;

    @GetMapping("/login")
    public String loginForm(Model model){
        model.addAttribute("loginForm", new LoginForm());
        return "user/loginForm";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("loginForm") LoginForm loginForm, BindingResult bindingResult, HttpServletRequest request){
        //1 필드검증
        if(bindingResult.hasErrors()){
            return "user/loginForm";
        }
        //2. 로그인 시도
        DoMember loginMember = loginService.login(loginForm.getLoginId(), loginForm.getPassword());

        //3.로그인 실패
        if(loginMember == null){
            bindingResult.reject("loginFail", "아이디 또는 비밀번호가 맞지 않습니다");
            return "user/loginForm";
        }

        //로그인 성공시 세션이 있으면 있는 세션 반환, 없으면 신규 세션 생성
        HttpSession session = request.getSession();
        log.info("세션을 찍어보자 {}", session);

        session.setAttribute(SessionConst.LOGIN_MEMBER, loginMember);

        log.info("누가 로그인 했을까? {} {}", loginMember.getLoginId(), loginMember.getName());
        return "redirect:/";
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request){

        //세션을 가져오는데 없으면 다시 생성하지 않는다.
        HttpSession session = request.getSession(false);

        if(session != null){
            session.invalidate();  //세션을 제거한다.
        }
        return "redirect:/";
    }


}
