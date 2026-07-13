package com.example.login.controller;

import com.example.login.domain.Answer;
import com.example.login.domain.DoMember;
import com.example.login.domain.Question;
import com.example.login.dto.AnswerForm;
import com.example.login.service.AnswerService;
import com.example.login.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class AnswerController {

    private final QuestionService questionService;
    private final AnswerService answerService;

    //답변 등록
    @PostMapping("/answer/create/{questionId}")
    public String createAnswer(
            @PathVariable("questionId") Long questionId,
            @Valid @ModelAttribute("answerForm") AnswerForm answerForm,
            BindingResult bindingResult,
            @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) DoMember loginMember,
            Model model) {

        Question question = questionService.getQuestion(questionId).orElseThrow();

        if (bindingResult.hasErrors()) {                    // 빈 답변 → 상세페이지 재표시
            model.addAttribute("question", question);       // 질문 데이터 다시 담기
            model.addAttribute("loginMember", loginMember);
            return "user/questionDetail";
        }

        Answer answer = new Answer();
        answer.setContent(answerForm.getContent());
        answer.setCreateDate(LocalDateTime.now());
        answer.setQuestion(question);                       // 어느 질문의 답변인지
        answer.setAuthor(loginMember);                      // 작성자 = 세션 사용자
        answerService.create(answer);

        return "redirect:/question/detail/" + questionId;   // 그 질문 상세로
    }
}