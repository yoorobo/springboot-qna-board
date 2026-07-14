package com.example.login.controller;

import com.example.login.domain.DoMember;
import com.example.login.dto.MemberForm;
import com.example.login.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/add")
    public String creatForm(Model model){
        model.addAttribute("memberForm", new MemberForm());
        return "user/addMemberForm";
    }

    @PostMapping("/add")
    public String create(@Valid @ModelAttribute("memberForm") MemberForm memberForm, BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            return "user/addMemberForm";
        }
        DoMember doMember = new DoMember();
        doMember.setLoginId(memberForm.getLoginId());
        doMember.setPassword(passwordEncoder.encode(memberForm.getPassword()));
        doMember.setName(memberForm.getName());
        doMember.setGrade("user");
        memberService.join(doMember);
        return "redirect:/";
    }

    @GetMapping("/members")
    public String list(Model model){
        List<DoMember> members = memberService.findMemberList();
        model.addAttribute("members", members);
        return "admin/memberList";
    }

    //수정폼 열기, http://localhost:8080/members/3/edit
    @GetMapping("/members/{memberId}/edit")
    public String updateMemberForm(
            @PathVariable("memberId") Long memberId, Model model){
        DoMember findMember = memberService.findOneMember(memberId).orElseThrow();

        MemberForm memberForm = new MemberForm();
        memberForm.setId(findMember.getId());
        memberForm.setLoginId(findMember.getLoginId());
        memberForm.setName(findMember.getName());
        memberForm.setPassword(findMember.getPassword());
        memberForm.setGrade(findMember.getGrade());
        model.addAttribute("memberForm", memberForm);
        return "admin/updateMemberForm";
    }

    //멤버 수정 저장
    @PostMapping("/members/{memberId}/edit")
    public String updateMemberSave(
            @Valid @ModelAttribute("memberForm") MemberForm memberForm,  BindingResult bindingResult, Model model){

        if(bindingResult.hasErrors()){
            return "admin/updateMemberForm";
        }

        DoMember doMember = new DoMember();

        doMember.setId(memberForm.getId());
        doMember.setLoginId(memberForm.getLoginId());
        doMember.setName(memberForm.getName());
        doMember.setPassword(passwordEncoder.encode(memberForm.getPassword()));
        doMember.setGrade(memberForm.getGrade());
        memberService.save(doMember);
        return "redirect:/members";
    }

    //삭제
    @GetMapping("/members/{memberId}/delete")
    public String delete(@PathVariable("memberId") Long memberId){

        DoMember findMember = memberService.findOneMember(memberId).orElseThrow();

        memberService.delete(findMember.getId());
        return "redirect:/members";
    }
}
