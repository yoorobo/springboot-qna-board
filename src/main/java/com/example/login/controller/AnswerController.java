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

    //답변 수정 폼 열기
    @GetMapping("/answer/modify/{id}")
    public String answerModifyForm(
            @PathVariable("id") Long id,
            @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) DoMember loginMember,
            Model model) {

        Answer answer = answerService.getAnswer(id).orElseThrow();

        // 기존 답변 내용을 폼에 채워서 전달
        AnswerForm answerForm = new AnswerForm();
        answerForm.setContent(answer.getContent());

        model.addAttribute("answerForm", answerForm);
        model.addAttribute("loginMember", loginMember);
        return "user/answerForm";
    }

    //답변 수정 저장
    @PostMapping("/answer/modify/{id}")
    public String answerModify(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("answerForm") AnswerForm answerForm,
            BindingResult bindingResult,
            @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) DoMember loginMember,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("loginMember", loginMember);
            return "user/answerForm";
        }

        Answer answer = answerService.getAnswer(id).orElseThrow();
        answerService.modify(answer, answerForm.getContent());

        // 답변엔 자기 페이지가 없음 → 소속 질문의 상세로 복귀
        return "redirect:/question/detail/" + answer.getQuestion().getId();
    }

    //답변 삭제
    @GetMapping("/answer/delete/{id}")
    public String answerDelete(
            @PathVariable("id") Long id,
            @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) DoMember loginMember) {

        Answer answer = answerService.getAnswer(id).orElseThrow();
        Long questionId = answer.getQuestion().getId();   // 삭제 전에 질문 id 확보!
        answerService.delete(answer);

        return "redirect:/question/detail/" + questionId;
    }
}