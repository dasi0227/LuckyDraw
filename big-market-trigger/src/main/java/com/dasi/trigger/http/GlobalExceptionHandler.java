package com.dasi.trigger.http;

import com.dasi.types.exception.AppException;
import com.dasi.types.exception.BusinessException;
import com.dasi.types.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException ex) {
        log.error("【业务异常】", ex);
        return Result.error(ex.getMessage());
    }

    @ExceptionHandler(AppException.class)
    public Result<Void> handleAppException(AppException ex) {
        log.error("【程序异常】", ex);
        return Result.error();
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception ex) {
        log.error("【系统异常】", ex);
        return Result.error();
    }
}
