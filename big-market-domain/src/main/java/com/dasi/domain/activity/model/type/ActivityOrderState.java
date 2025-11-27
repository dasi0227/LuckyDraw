package com.dasi.domain.activity.model.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ActivityOrderState {

    CREATED("created"),
    COMPLETED("completed"),
    ;

    private final String code;

}