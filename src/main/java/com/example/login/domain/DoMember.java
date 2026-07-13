package com.example.login.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter @Setter
public class DoMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    private String loginId;

    private String name;

    private String password;

    private String grade;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL)
    private List<Question> questionList;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL)
    private List<Answer> answerList;
}
