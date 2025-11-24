package com.dasi.types.model;

import com.dasi.types.enumeration.ResultInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 7000723935764546321L;

    private String code;
    private String info;
    private T data;

    public static <T> Result<T> success() {
        return build(null, ResultInfo.SUCCESS.getCode(), ResultInfo.SUCCESS.getInfo());
    }

    public static <T> Result<T> success(T data) {
        return build(data, ResultInfo.SUCCESS.getCode(), ResultInfo.SUCCESS.getInfo());
    }

    public static <T> Result<T> error() {
        return build(null, ResultInfo.ERROR.getCode(), ResultInfo.ERROR.getInfo());
    }

    public static <T> Result<T> error(String info) {
        return build(null, ResultInfo.ERROR.getCode(), info);
    }

    private static <T> Result<T> build(T data, String code, String info) {
        return Result.<T>builder()
                .data(data)
                .code(code)
                .info(info)
                .build();
    }

}
