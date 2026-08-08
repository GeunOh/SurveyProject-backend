package com.surveyplus.creator.user.dto.response;

import com.surveyplus.creator.user.entity.Member;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberResponse {

    private Long id;
    private String email;
    private String nickname;
    private String provider;

    public static MemberResponse from(Member user) {
        return MemberResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .provider(user.getProvider())
                .build();
    }
}
