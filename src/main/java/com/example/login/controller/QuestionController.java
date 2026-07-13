package com.example.login.controller;

import com.example.login.domain.DoMember;
import com.example.login.domain.Question;
import com.example.login.dto.AnswerForm;
import com.example.login.dto.QuestionForm;
import com.example.login.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

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

    //목록보기 (페이징 + 검색)
    @GetMapping("/question/list")
    public String list(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "kw", defaultValue = "") String kw,
            @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) DoMember loginMember,
            Model model) {

        Page<Question> paging = questionService.getList(page, kw);
        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);          // 검색 후에도 입력창에 검색어 유지용
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