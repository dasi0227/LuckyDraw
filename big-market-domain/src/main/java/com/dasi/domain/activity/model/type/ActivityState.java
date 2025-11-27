package com.dasi.domain.activity.model.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ActivityState {

    CREATED("created"),
    UNDERWAY("underway"),
    OVER("over")
    ;

    private final String code;

}