package com.example.login.service;

import com.example.login.domain.Answer;
import com.example.login.domain.Question;
import com.example.login.domain.DoMember;
import com.example.login.repository.AnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AnswerService {

    private final AnswerRepository answerRepository;

    //답변 저장
    public void create(Question question, String content, DoMember author) {
        Answer answer = new Answer();
        answer.setContent(content);
        answer.setCreateDate(LocalDateTime.now());
        answer.setQuestion(question);   // 어느 질문의 답변인지
        answer.setAuthor(author);       // 세션 로그인 사용자
        answerRepository.save(answer);
    }
}