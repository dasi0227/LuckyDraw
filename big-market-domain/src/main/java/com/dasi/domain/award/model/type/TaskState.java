package com.dasi.domain.award.model.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TaskState {

    CREATED("created"),
    FAILED("failed"),
    SEND("send")

    ;

    private final String code;
}
