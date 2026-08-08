package com.surveyplus.creator.answer.controller;

import com.surveyplus.creator.answer.dto.request.QuestionDetailRequest;
import com.surveyplus.creator.answer.dto.request.SubmitAnswerRequest;
import com.surveyplus.creator.answer.dto.request.SurveyStartRequest;
import com.surveyplus.creator.answer.dto.response.SurveyIntroResponse;
import com.surveyplus.creator.answer.service.AnswerService;
import com.surveyplus.creator.global.exception.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/answer")
@RequiredArgsConstructor
public class AnswerController {

    private final AnswerService answerService;


    @GetMapping("/intro/{surveyId}")
    public ResponseEntity<?> getSurveyIntro(@PathVariable("surveyId") Long surveyId) {
        log.info("설문 인트로 및 세션 생성 요청: surveyId={}", surveyId);

        SurveyIntroResponse surveyIntro = answerService.getSurveyIntroAndCreateRandomId(surveyId);

        return ResponseEntity.ok(ApiResponse.success(surveyIntro));
    }

    @PostMapping("/start")
    public ResponseEntity<?> startSurvey(@RequestBody SurveyStartRequest surveyStartReq) {
        log.info("설문 응답 시작 요청: surveyId={}, randomId={}", surveyStartReq.getSurveyId(), surveyStartReq.getAnswerId());

        return ResponseEntity.ok(ApiResponse.success(answerService.startSurvey(surveyStartReq)));
    }

    @GetMapping("/questionDetail")
    public ResponseEntity<?> getQuestionDetail(@Valid QuestionDetailRequest questionDetailReq) {
        log.info("설문 문항 상세 조회 요청: surveyId={}, questionId={}, answerId={}", questionDetailReq.getSurveyId(), questionDetailReq.getQuestionId(), questionDetailReq.getAnswerId());

        return ResponseEntity.ok(ApiResponse.success(answerService.getQuestionDetail(questionDetailReq)));
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submitAnswer(@RequestBody SubmitAnswerRequest request) {
        return ResponseEntity.ok(ApiResponse.success(answerService.submitAnswerAndGetNext(request)));
    }
}