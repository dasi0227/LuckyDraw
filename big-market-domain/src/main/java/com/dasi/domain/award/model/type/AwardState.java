package com.dasi.domain.award.model.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AwardState {

    CREATED("created"),
    COMPLETED("completed"),
    FAILED("failed"),
    ;

    private final String code;

}
