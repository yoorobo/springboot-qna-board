package com.example.login.service;

import com.example.login.domain.Answer;
import com.example.login.repository.AnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnswerService {

    private final AnswerRepository answerRepository;

    //답변 저장
    public void create(Answer answer) {
        answerRepository.save(answer);
    }
}