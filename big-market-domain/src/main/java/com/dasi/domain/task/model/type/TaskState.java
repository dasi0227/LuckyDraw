package com.dasi.domain.task.model.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TaskState {

    CREATED("created"),
    FAILED("failed"),
    DISTRIBUTED("distributed")

    ;

    private final String code;
}
