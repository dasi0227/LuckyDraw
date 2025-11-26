package com.dasi.domain.activity.model.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ActivityState {

    CREATED("created"),
    OPEN("open"),
    CLOSE("close")
    ;

    private final String code;

}
