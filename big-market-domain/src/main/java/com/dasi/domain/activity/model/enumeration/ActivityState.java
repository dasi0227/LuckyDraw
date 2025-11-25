package com.dasi.domain.activity.model.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ActivityState {

    CREATED("created");

    private final String code;

}
