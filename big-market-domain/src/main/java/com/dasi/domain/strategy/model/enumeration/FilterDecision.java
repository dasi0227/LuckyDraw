package com.dasi.domain.strategy.model.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FilterDecision {

    ALLOW("0000", "放行：不受规则引擎影响"),
    TAKE_OVER("0001", "接管：受规则引擎影响"),
    ;

    private final String code;
    private final String info;

}
