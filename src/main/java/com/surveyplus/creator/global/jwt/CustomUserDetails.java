package com.surveyplus.creator.global.jwt;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.io.Serial;
import java.util.Collection;

@Getter
public class CustomUserDetails extends User {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Long memberId;

    public CustomUserDetails(Long memberId, String email, Collection<? extends GrantedAuthority> authorities) {
        super(email, "", authorities);
        this.memberId = memberId;
    }

}
