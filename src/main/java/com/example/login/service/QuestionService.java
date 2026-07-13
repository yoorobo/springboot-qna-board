package com.example.login.service;

import com.example.login.domain.Question;
import com.example.login.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;

    //질문생성시 저장하기
    public void create(Question question){
        questionRepository.save(question);
    }

    //전체질문조회
    public List<Question> getList(){
        return questionRepository.findAll();
    }

    //질문 1건 조회
    public Optional<Question> getQuestion(Long id){
        return questionRepository.findById(id);
    }
}
