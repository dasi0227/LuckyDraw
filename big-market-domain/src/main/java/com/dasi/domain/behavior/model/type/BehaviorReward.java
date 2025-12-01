package com.dasi.domain.behavior.model.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BehaviorReward {

    POINT("point"),
    SKU("sku")
    ;
    private final String code;

}
