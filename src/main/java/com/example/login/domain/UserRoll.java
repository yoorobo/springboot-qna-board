package com.example.login.domain;

import lombok.Getter;

@Getter                          // ← getValue() 자동 생성의 주인공
public enum UserRoll {

    //사용자 권한의 종류(ADMIN, USER)를 상수로 정의
    ADMIN("ROLE_ADMIN"),
    USER("ROLE_USER");

    private String value;

    UserRoll(String value) {     // ← 생성자명 = 클래스명(UserRoll)
        this.value = value;
    }
}