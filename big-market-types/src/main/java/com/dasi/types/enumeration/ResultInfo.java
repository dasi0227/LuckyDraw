package com.dasi.types.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum ResultInfo {

    SUCCESS("0000", "成功"),
    ERROR("0001", "失败"),
    ILLEGAL("0002", "非法请求"),
    NOT_FOUND("0003", "数据库查询不到")

    ;

    private String code;
    private String info;

}
