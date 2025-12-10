package com.dasi.types.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum ResultInfo {

    SUCCESS("200", "成功"),
    ERROR("500", "失败")

    ;

    private String code;
    private String info;

}
