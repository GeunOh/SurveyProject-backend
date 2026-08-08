package com.surveyplus.creator.global.jwt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TokenMemberInfo {
    private Long id;
    private String email;
    private String name;
}
