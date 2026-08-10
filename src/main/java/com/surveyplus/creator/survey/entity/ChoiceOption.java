package com.surveyplus.creator.survey.entity;

import com.surveyplus.creator.survey.dto.response.ChoiceOptionResponse;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "choice_option")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ChoiceOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "choice_id", nullable = false)
    private Choice choice;

    @Column(name = "option_key", nullable = false, length = 50)
    private String key;

    @Column(name = "option_value")
    private String value;

    public void assignChoice(Choice choice) {
        this.choice = choice;
    }

    public ChoiceOptionResponse from() {
        return ChoiceOptionResponse.builder()
                .id(this.id)
                .key(this.key)
                .value(this.value)
                .build();
    }

    public void updateOption(String value) {
        this.value = value;
    }
}
