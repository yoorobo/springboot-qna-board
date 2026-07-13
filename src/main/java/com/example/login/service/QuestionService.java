package com.example.login.service;

import com.example.login.domain.Answer;
import com.example.login.domain.DoMember;
import com.example.login.domain.Question;
import com.example.login.repository.QuestionRepository;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;

    //질문생성시 저장하기
    public void create(Question question){
        questionRepository.save(question);
    }

    //전체질문조회 (페이징 + 검색)
    public Page<Question> getList(int page, String kw){
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createDate"));

        if (kw == null || kw.trim().isEmpty()) {
            return questionRepository.findAll(pageable);        // 검색어 없음 → 전체
        }

        Specification<Question> spec = search(kw);
        return questionRepository.findAll(spec, pageable);      // 검색어 있음 → 조건 검색
    }

    //검색 조건 생성 (제목/내용/질문작성자/답변내용/답변작성자)
    private Specification<Question> search(String kw) {
        return new Specification<Question>() {
            @Override
            public Predicate toPredicate(Root<Question> q,
                                         CriteriaQuery<?> query,
                                         CriteriaBuilder cb) {
                query.distinct(true);   // 답변 여러 개 → 질문 중복 제거

                Join<Question, DoMember> m1 = q.join("author", JoinType.LEFT);      // 질문 작성자
                Join<Question, Answer> a = q.join("answerList", JoinType.LEFT);    // 답변
                Join<Answer, DoMember> m2 = a.join("author", JoinType.LEFT);       // 답변 작성자

                return cb.or(
                        cb.like(q.get("subject"), "%" + kw + "%"),
                        cb.like(q.get("content"), "%" + kw + "%"),
                        cb.like(m1.get("loginId"), "%" + kw + "%"),
                        cb.like(a.get("content"), "%" + kw + "%"),
                        cb.like(m2.get("loginId"), "%" + kw + "%")
                );
            }
        };
    }

    //질문 1건 조회
    public Optional<Question> getQuestion(Long id){
        return questionRepository.findById(id);
    }

    //질문 수정
    public void modify(Question question, String subject, String content){
        question.setSubject(subject);
        question.setContent(content);
        question.setModifyDate(LocalDateTime.now());   // 수정 시각 기록
        questionRepository.save(question);             // 같은 id → update
    }

    //질문 삭제 (연관 답변도 함께 삭제됨 — cascade)
    public void delete(Question question) {
        questionRepository.delete(question);
    }
}