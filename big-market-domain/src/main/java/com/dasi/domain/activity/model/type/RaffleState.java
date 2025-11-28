package com.dasi.domain.activity.model.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RaffleState {

    CREATED("created"),
    USED("used"),
    CANCELLED("cancelled"),
    ;

    private final String code;

}
