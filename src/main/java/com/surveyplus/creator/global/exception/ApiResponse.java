package com.surveyplus.creator.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<D> {
    private final String code;
    private final String message;
    private final D data; // 성공 시 데이터를 담을 필드 추가

    // 1. 성공 시 데이터가 있는 경우
    public static <D> ApiResponse<D> success(D data) {
        return new ApiResponse<>("SU", "성공", data);
    }

    // 2. 성공 시 데이터가 없는 경우 (예: 회원가입 성공)
    public static ApiResponse<Void> success() {
        return new ApiResponse<>("SU", "성공", null);
    }

    // 3. 실패 시 (기존 ErrorResponse 역할)
    public static ApiResponse<Void> error(String code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
