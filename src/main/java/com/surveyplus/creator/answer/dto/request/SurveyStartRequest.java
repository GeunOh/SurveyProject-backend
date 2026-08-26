package com.surveyplus.creator.answer.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SurveyStartRequest {
    // 공유 링크에 담긴 서명 토큰 (숫자 설문 ID/테스트 여부를 클라이언트가 직접 신고하지 않고, 서버가 토큰을 재검증해서 판단)
    private String token;
    private String answerId;
}
