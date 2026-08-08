package com.surveyplus.creator.user;

import com.surveyplus.creator.user.entity.Member;
import com.surveyplus.creator.user.dto.request.SignUpRequest;
import com.surveyplus.creator.user.exception.MemberException;
import com.surveyplus.creator.user.repository.MemberRepository;
import com.surveyplus.creator.user.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberService memberService;

    @Test
    @DisplayName("회원가입 성공 테스트")
    void signUpSuccess() {
        // given
        SignUpRequest request = SignUpRequest.builder()
                .email("test@test.com")
                .password("pass1234!")
                .nickname("nick")
                .provider("LOCAL")
                .build();

        // 1. 중복 체크는 false
        when(memberRepository.existsByEmail(any())).thenReturn(false);
        // 2. 암호화는 문자열 반환
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        // 3. [핵심] save 호출 시 저장할 엔티티를 그대로 반환하도록 설정
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        memberService.signUp(request);

        // then
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    @DisplayName("이메일 중복 시 예외 발생 테스트")
    void signUpFailByDuplicateEmail() {
        // given
        SignUpRequest request = SignUpRequest.builder()
                .email("test@test.com")
                .password("pass1234!")
                .nickname("nick")
                .provider("LOCAL")
                .build();
        when(memberRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // when & then
        assertThrows(MemberException.class, () -> memberService.signUp(request));
    }
}
