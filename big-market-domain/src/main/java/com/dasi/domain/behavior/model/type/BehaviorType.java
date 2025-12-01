package com.dasi.domain.behavior.model.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BehaviorType {

    SIGN("sign"),
    LIKE("like")
    ;

    private final String code;

}
