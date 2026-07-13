package com.example.login.service;

import com.example.login.domain.Answer;
import com.example.login.repository.AnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AnswerService {

    private final AnswerRepository answerRepository;

    //답변 저장
    public void create(Answer answer) {
        answerRepository.save(answer);
    }

    //답변 1건 조회
    public Optional<Answer> getAnswer(Long id) {
        return answerRepository.findById(id);
    }

    //답변 수정
    public void modify(Answer answer, String content) {
        answer.setContent(content);
        answer.setModifyDate(LocalDateTime.now());   // 수정 시각 기록
        answerRepository.save(answer);               // 같은 id → update
    }

    //답변 삭제
    public void delete(Answer answer) {
        answerRepository.delete(answer);
    }
}