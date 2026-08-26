package com.surveyplus.creator.answer.util;

public class SurveyTimeCalculator {

    public static int getQuestionSeconds(String type) {
        if (type == null) {
            return 30; // 기본값
        }

        return switch (type) {
            case "A01" -> 20; // 단답형
            case "A02" -> 40; // 장문형
            case "A03" -> 15; // 객관식(택1)
            case "A04" -> 30; // 객관식(복수택)
            case "A05" -> 20; // 척도형
            case "A06" -> 15; // 별점형
            default -> 30;
        };
    }
}
