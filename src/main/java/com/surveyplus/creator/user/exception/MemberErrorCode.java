package com.surveyplus.creator.user.exception;

import com.surveyplus.creator.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MemberErrorCode implements BaseErrorCode {
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "UE001", "아이디 또는 비밀번호가 올바르지 않습니다"),
    EMAIL_DUPLICATED(HttpStatus.CONFLICT, "UE002", "이미 존재하는 이메일입니다.");


    private final HttpStatus status;
    private final String code;
    private final String message;
}
