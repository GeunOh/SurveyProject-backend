package com.surveyplus.creator.survey.controller;

import com.surveyplus.creator.global.exception.ApiResponse;
import com.surveyplus.creator.survey.dto.request.SurveySaveRequest;
import com.surveyplus.creator.survey.dto.request.SurveySearchCondition;
import com.surveyplus.creator.survey.dto.request.SurveyStatusUpdateRequest;
import com.surveyplus.creator.survey.dto.response.SurveyListResponse;
import com.surveyplus.creator.survey.dto.response.SurveyResponse;
import com.surveyplus.creator.survey.enums.SurveyStatus;
import com.surveyplus.creator.survey.service.SurveyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/survey")
@RequiredArgsConstructor
public class SurveyController {

    private final SurveyService surveyService;

    @PostMapping("/create")
    public ResponseEntity<?> createSurvey(@RequestBody SurveySaveRequest surveyRequest) {

        log.info("설문 생성 요청 시작: surveyInfo={}", surveyRequest);

        Long surveyId = surveyService.createSurvey(surveyRequest);

        // 2. 요청 성공 로그
        log.info("사용자 {}가 설문을 생성 중: {}", surveyRequest.getMemberId(), surveyId);

        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/list")
    public ResponseEntity<?> getSurveyList(
            @RequestParam("memberId") Long memberId,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) SurveyStatus status,
            @PageableDefault(size = 6, page = 0) Pageable pageable
    ) {
        log.info("회원 ID {}의 설문 리스트 조회 요청", memberId);

        SurveySearchCondition surveyCondition = SurveySearchCondition.of(memberId, keyword, status);
        Page<SurveyListResponse> surveyList = surveyService.getSurveyList(surveyCondition, pageable);


        return ResponseEntity.ok(ApiResponse.success(surveyList));
    }

    @PatchMapping("/{surveyId}/status")
    public ResponseEntity<?> updateSurveyStatus(
            @PathVariable("surveyId") Long surveyId,
            @RequestBody SurveyStatusUpdateRequest surveyStatusReq
    ) {
        surveyService.updateSurveyStatus(surveyId, surveyStatusReq);

        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/{surveyId}")
    public ResponseEntity<?> getSurveyDetail(@PathVariable("surveyId") Long surveyId) {
        SurveyResponse response = surveyService.getSurveyDetail(surveyId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{surveyId}")
    public ResponseEntity<ApiResponse<Void>> updateSurvey(
            @PathVariable("surveyId") Long surveyId,
            @Valid @RequestBody SurveySaveRequest surveyReq
    ) {
        surveyService.updateSurvey(surveyId, surveyReq);

        return ResponseEntity.ok(ApiResponse.success());
    }

    @DeleteMapping("/{surveyId}")
    public ResponseEntity<?> deleteSurvey(@PathVariable("surveyId") Long surveyId) {
        surveyService.deleteSurvey(surveyId);

        return ResponseEntity.ok(ApiResponse.success());
    }
}
