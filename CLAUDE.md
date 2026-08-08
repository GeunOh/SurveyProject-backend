# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 프로젝트 개요

Spring Boot 3.5.8 (Java 17) 기반 설문조사(Survey) 서비스의 백엔드. Gradle 멀티 모듈이 아닌 단일 모듈 프로젝트이며, 루트 프로젝트 이름은 `survey`, 베이스 패키지는 `com.surveyplus.creator`.

## 자주 사용하는 명령어

Windows 환경이므로 `gradlew.bat`을 사용한다 (Git Bash에서는 `./gradlew`도 동작).

```bash
# 빌드
./gradlew build

# 애플리케이션 실행 (포트 5170)
./gradlew bootRun

# 전체 테스트 실행
./gradlew test

# 단일 테스트 클래스 실행
./gradlew test --tests "com.surveyplus.creator.user.MemberServiceTest"

# 단일 테스트 메서드 실행
./gradlew test --tests "com.surveyplus.creator.user.MemberServiceTest.메서드명"

# QueryDSL Q-클래스 재생성 (엔티티 변경 후 IDE에서 Q클래스가 안 보일 때)
./gradlew clean compileJava
```

- QueryDSL이 생성하는 `Q*` 클래스는 `src/main/generated`에 위치한다. 엔티티 필드를 변경했는데 컴파일 에러가 나면 먼저 `clean compileJava`로 재생성부터 의심할 것.
- 로컬 DB는 MySQL이며 접속 정보는 `src/main/resources/application.properties`에 평문으로 들어있다 (DB `survey`, 포트 3306). 로컬 개발용 자격증명이므로 실제 운영 값이 아닌지 확인 후 다루되, 커밋 시 민감정보 노출에 주의할 것.

## 아키텍처

### 패키지 구조 (도메인 단위 수직 슬라이스)

기능별(계층별이 아니라 도메인별)로 패키지가 나뉘어 있고, 각 도메인 패키지 내부는 `controller / service / repository / entity / dto(request,response) / exception / enums` 하위 구조를 반복한다.

- `auth` — 로그인/토큰 재발급 (`AuthController`, `AuthService`)
- `user` — 회원가입/회원 (`Member` 엔티티, `MemberController`)
- `survey` — 설문 생성/조회/수정/삭제, 질문(`Question`)·보기(`Choice`, `ChoiceOption`, `SurveyOption`) 관리. QueryDSL 동적 검색은 `SurveyQueryRepository`/`SurveyQueryRepositoryImpl`에 위치 (`SurveySearchCondition` 기반 페이징 조회)
- `answer` — 설문 응답 제출/조회 (`ResponseAnswer`, `ResponseStatus`, `AnswerService`). 익명 응답자도 접근 가능한 공개 API
- `global` — 도메인 공통 인프라: `config`(Security, QueryDSL, Web), `jwt`, `exception`, `util`

### 인증/인가 (JWT, Stateless)

- `SecurityConfig`에서 세션을 `STATELESS`로 설정하고 `JwtFilter`를 `UsernamePasswordAuthenticationFilter` 앞에 등록.
- `TokenProvider`가 Access Token(30분)/Refresh Token(7일)을 발급·검증한다. 비밀키는 `application.properties`의 `jwt.secret` (Base64) 사용, 서명 알고리즘은 HMAC.
- 인증 실패는 `JwtAuthenticationEntryPoint`(401), 인가 실패는 `JwtAccessDeniedHandler`(403)가 처리.
- 인증 없이 접근 가능한 엔드포인트는 `SecurityConfig.filterChain`에 명시: `/api/v1/users/login`, `/api/v1/users/signup`, `/api/v1/auth/refresh`, `/api/v1/answer/**` (응답 제출은 익명 사용자를 위해 공개). 새 공개 API를 추가하려면 이 whitelist를 함께 수정해야 한다.

### 공통 응답/예외 처리 패턴

모든 컨트롤러는 `ApiResponse<D>`(`code`, `message`, `data`)로 응답을 감싼다. 성공은 `ApiResponse.success()` / `ApiResponse.success(data)`를 사용.

에러 코드는 도메인별 enum이 `BaseErrorCode`(status, code, message)를 구현하는 패턴을 따른다 (예: `SurveyErrorCode`, `MemberErrorCode`, `AnswerErrorCode`, `QuestionErrorCode`, `TokenErrorCode`). 도메인 전용 런타임 예외(`SurveyException` 등)는 해당 `BaseErrorCode`를 들고 있고, `GlobalExceptionHandler`(`@RestControllerAdvice`)가 도메인별로 `@ExceptionHandler`를 두어 `errorCode`에서 status/code/message를 꺼내 `ApiResponse`로 변환한다.

새 도메인 예외를 추가할 때는:
1. 해당 도메인 패키지의 `exception/`에 `XxxErrorCode implements BaseErrorCode` enum과 `XxxException` 추가
2. `GlobalExceptionHandler`에 전용 `@ExceptionHandler(XxxException.class)` 핸들러 추가 (기존 도메인들과 동일한 패턴 복사)

### 엔티티/도메인 모델 관례

- 엔티티는 Lombok `@Getter`, `@Builder`, `@NoArgsConstructor(PROTECTED)`, `@AllArgsConstructor`를 쓰고 세터 없이 도메인 메서드(`changeStatus`, `deleteSurvey`, `updateInfo` 등)로 상태를 변경한다.
- 소프트 삭제 패턴 사용: `isDeleted` 플래그 + `deletedAt` 타임스탬프 (물리 삭제 아님).
- `@CreatedDate`/`@LastModifiedDate` + `AuditingEntityListener`로 생성/수정 시각 자동 관리.
- 엔티티에 `from()` / `fromXxx()` 형태의 변환 메서드를 두어 자신을 Response DTO로 매핑한다 (별도 매퍼 클래스를 쓰지 않음).
- 연관관계는 `@OneToMany` + `@JoinColumn`(FK 컬럼 직접 지정) 조합을 쓰고, `@ManyToOne` 대신 ID(`memberId`, `surveyId`)를 컬럼으로만 들고 있는 경우가 많다 — JPA 연관관계 매핑이 아니라 ID 참조로 도메인 간 결합도를 낮추는 방식.

### QueryDSL

동적 검색/페이징이 필요한 조회는 Spring Data JPA 리포지토리 대신 QueryDSL(`JPAQueryFactory`, `QueryDslConfig`에서 빈 등록)을 쓰는 `XxxQueryRepository` + `XxxQueryRepositoryImpl` 패턴을 따른다 (예: `SurveyQueryRepository`/`SurveyQueryRepositoryImpl`, 검색 조건은 `SurveySearchCondition`). 새로운 동적 조회를 추가할 때 이 패턴을 따를 것.
