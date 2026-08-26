package com.surveyplus.creator.survey.controller;

import com.surveyplus.creator.global.exception.ApiResponse;
import com.surveyplus.creator.global.jwt.CustomUserDetails;
import com.surveyplus.creator.survey.dto.request.SurveySaveRequest;
import com.surveyplus.creator.survey.dto.request.SurveySearchCondition;
import com.surveyplus.creator.survey.dto.request.SurveyStatusUpdateRequest;
import com.surveyplus.creator.survey.dto.response.HeatmapCellResponse;
import com.surveyplus.creator.survey.dto.response.RecentResponseItem;
import com.surveyplus.creator.survey.dto.response.ResponseTimeStatsResponse;
import com.surveyplus.creator.survey.dto.response.ResponseTrendItem;
import com.surveyplus.creator.survey.dto.response.ShareTokenResponse;
import com.surveyplus.creator.survey.dto.response.StatsComparisonResponse;
import com.surveyplus.creator.survey.dto.response.SurveyListResponse;
import com.surveyplus.creator.survey.dto.response.SurveyResponse;
import com.surveyplus.creator.survey.dto.response.SurveyStatsResponse;
import com.surveyplus.creator.survey.enums.SurveyStatus;
import com.surveyplus.creator.survey.service.SurveyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/survey")
@RequiredArgsConstructor
public class SurveyController {

    private final SurveyService surveyService;

    @PostMapping("/create")
    public ResponseEntity<?> createSurvey(
            @RequestBody SurveySaveRequest surveyRequest,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {

        log.info("설문 생성 요청 시작: surveyInfo={}", surveyRequest);

        Long surveyId = surveyService.createSurvey(surveyRequest, principal.getMemberId());

        // 2. 요청 성공 로그
        log.info("사용자 {}가 설문을 생성 중: {}", principal.getMemberId(), surveyId);

        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/list")
    public ResponseEntity<?> getSurveyList(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) SurveyStatus status,
            @PageableDefault(size = 6) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        log.info("회원 ID {}의 설문 리스트 조회 요청", principal.getMemberId());

        SurveySearchCondition surveyCondition = SurveySearchCondition.of(principal.getMemberId(), keyword, status);
        Page<SurveyListResponse> surveyList = surveyService.getSurveyList(surveyCondition, pageable);


        return ResponseEntity.ok(ApiResponse.success(surveyList));
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getSurveyStats(
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        SurveyStatsResponse stats = surveyService.getSurveyStats(principal.getMemberId());

        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/stats/response-trend")
    public ResponseEntity<?> getResponseTrend(
            @RequestParam(value = "days", defaultValue = "30") int days,
            @RequestParam(value = "surveyId", required = false) Long surveyId,
            @RequestParam(value = "includeTestData", defaultValue = "false") boolean includeTestData,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        List<ResponseTrendItem> trend = surveyService.getResponseTrend(principal.getMemberId(), days, surveyId, includeTestData);

        return ResponseEntity.ok(ApiResponse.success(trend));
    }

    @GetMapping("/stats/recent-responses")
    public ResponseEntity<?> getRecentResponses(
            @RequestParam(value = "size", defaultValue = "5") int size,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        List<RecentResponseItem> recentResponses = surveyService.getRecentResponses(principal.getMemberId(), size);

        return ResponseEntity.ok(ApiResponse.success(recentResponses));
    }

    @GetMapping("/stats/heatmap")
    public ResponseEntity<?> getHeatmap(
            @RequestParam(value = "days", defaultValue = "30") int days,
            @RequestParam(value = "surveyId", required = false) Long surveyId,
            @RequestParam(value = "includeTestData", defaultValue = "false") boolean includeTestData,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        List<HeatmapCellResponse> heatmap = surveyService.getHeatmap(principal.getMemberId(), days, surveyId, includeTestData);

        return ResponseEntity.ok(ApiResponse.success(heatmap));
    }

    @GetMapping("/stats/response-time")
    public ResponseEntity<?> getResponseTimeStats(
            @RequestParam(value = "days", defaultValue = "30") int days,
            @RequestParam(value = "surveyId", required = false) Long surveyId,
            @RequestParam(value = "includeTestData", defaultValue = "false") boolean includeTestData,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        ResponseTimeStatsResponse responseTimeStats = surveyService.getResponseTimeStats(principal.getMemberId(), days, surveyId, includeTestData);

        return ResponseEntity.ok(ApiResponse.success(responseTimeStats));
    }

    @GetMapping("/stats/comparison")
    public ResponseEntity<?> getStatsComparison(
            @RequestParam(value = "days", defaultValue = "30") int days,
            @RequestParam(value = "surveyId", required = false) Long surveyId,
            @RequestParam(value = "includeTestData", defaultValue = "false") boolean includeTestData,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        StatsComparisonResponse comparison = surveyService.getStatsComparison(principal.getMemberId(), days, surveyId, includeTestData);

        return ResponseEntity.ok(ApiResponse.success(comparison));
    }

    @PatchMapping("/{surveyId}/status")
    public ResponseEntity<?> updateSurveyStatus(
            @PathVariable("surveyId") Long surveyId,
            @RequestBody SurveyStatusUpdateRequest surveyStatusReq,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        surveyService.updateSurveyStatus(surveyId, surveyStatusReq, principal.getMemberId());

        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/{surveyId}")
    public ResponseEntity<?> getSurveyDetail(
            @PathVariable("surveyId") Long surveyId,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        SurveyResponse response = surveyService.getSurveyDetail(surveyId, principal.getMemberId());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{surveyId}")
    public ResponseEntity<ApiResponse<Void>> updateSurvey(
            @PathVariable("surveyId") Long surveyId,
            @Valid @RequestBody SurveySaveRequest surveyReq,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        surveyService.updateSurvey(surveyId, surveyReq, principal.getMemberId());

        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/{surveyId}/share-token")
    public ResponseEntity<?> getShareToken(
            @PathVariable("surveyId") Long surveyId,
            @RequestParam(value = "test", required = false, defaultValue = "false") boolean isTest,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        String token = surveyService.getShareToken(surveyId, principal.getMemberId(), isTest);

        return ResponseEntity.ok(ApiResponse.success(new ShareTokenResponse(token)));
    }

    @DeleteMapping("/{surveyId}")
    public ResponseEntity<?> deleteSurvey(
            @PathVariable("surveyId") Long surveyId,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        surveyService.deleteSurvey(surveyId, principal.getMemberId());

        return ResponseEntity.ok(ApiResponse.success());
    }
}
