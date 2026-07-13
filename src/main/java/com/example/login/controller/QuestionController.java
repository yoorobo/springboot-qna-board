package com.example.login.controller;

import com.example.login.controller.SessionConst;
import com.example.login.domain.DoMember;
import com.example.login.domain.Question;
import com.example.login.dto.AnswerForm;
import com.example.login.dto.QuestionForm;
import com.example.login.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    //질문등록폼 열기
    @GetMapping("/question/create")
    public String questionCreateForm(@SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) DoMember loginMember, Model model) {
        model.addAttribute("questionForm", new QuestionForm());
        model.addAttribute("loginMember", loginMember);
        return "user/questionForm";
    }

    //질문 저장
    @PostMapping("/question/create")
    public String questionCreate(
            @Valid @ModelAttribute("questionForm") QuestionForm questionForm,
            BindingResult bindingResult,
            @SessionAttribute(name = SessionConst.LOGIN_MEMBER) DoMember loginMember,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("loginMember", loginMember);
            return "user/questionForm";
        }

        Question question = new Question();
        question.setSubject(questionForm.getSubject());
        question.setContent(questionForm.getContent());
        question.setCreateDate(LocalDateTime.now());
        question.setAuthor(loginMember);

        questionService.create(question);

        return "redirect:/question/list";
    }

    //목록보기
    @GetMapping("/question/list")
    public String list(
            @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) DoMember loginMember,
            Model model) {

        List<Question> questionList = questionService.getList();
        model.addAttribute("questionList", questionList);
        model.addAttribute("loginMember", loginMember);

        return "user/questionList";
    }

    //상세페이지
    @GetMapping("/question/detail/{id}")
    public String detail(
            @PathVariable("id") Long id,
            @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) DoMember loginMember,
            Model model) {

        Question question = questionService.getQuestion(id).orElseThrow();
        model.addAttribute("question", question);
        model.addAttribute("answerForm", new AnswerForm());
        model.addAttribute("loginMember", loginMember);

        return "user/questionDetail";
    }
}