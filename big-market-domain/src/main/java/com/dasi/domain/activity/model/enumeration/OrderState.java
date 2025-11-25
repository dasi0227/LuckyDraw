package com.dasi.domain.activity.model.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderState {

    COMPLETED("completed"),;

    private final String code;

}