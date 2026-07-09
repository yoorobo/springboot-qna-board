package com.example.login.controller;

import com.example.login.domain.DoMember;
import com.example.login.dto.MemberForm;
import com.example.login.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;   // ← 이 줄 추가
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/add")
    public String createForm(Model model) {
        model.addAttribute("memberForm", new MemberForm());
        return "user/addMemberForm";
    }

    @GetMapping("/members")
    public String list(Model model) {
        List<DoMember> members = memberService.findMemberList();
        model.addAttribute("members", members);
        return "admin/memberList";
    }
    //수정폼 열기
    @GetMapping("/members/{memberId}/edit")
    public String updateMemberForm(@PathVariable("memberId") Long memberId, Model model) {
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
    @PostMapping("/add")
    public String create(@Valid @ModelAttribute("memberForm") MemberForm memberForm, BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            return "user/addMemberForm";
        }
        DoMember doMember = new DoMember();
        doMember.setLoginId(memberForm.getLoginId());
        doMember.setPassword(memberForm.getPassword());
        doMember.setName(memberForm.getName());
        doMember.setGrade("User");
        memberService.join(doMember);
        return "redirect:/";
    }

   //멤버 수정 저장
   @PostMapping("/members/{memberId}/edit")
   public String updateMemberSave(@Valid @ModelAttribute("memberForm") MemberForm memberForm, BindingResult bindingResult) {

        if(bindingResult.hasErrors()) {
            return "admin/updateMemberForm";
        }

       DoMember doMember = memberService.findOneMember(memberForm.getId()).orElseThrow();

       doMember.setName(memberForm.getName());
       doMember.setGrade(memberForm.getGrade());

       if (memberForm.getPassword() != null && !memberForm.getPassword().isBlank()) {
           doMember.setPassword(memberForm.getPassword());
       }

       memberService.save(doMember);   // Service가 save니까 save 그대로
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
