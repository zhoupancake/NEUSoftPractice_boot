package com.system.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;


//全局异常处理

@RestControllerAdvice
// 全局异常处理器，用于捕获和处理异常

public class GlobalExceptionHandler {
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(Exception.class)
    // @ExceptionHandler注解表示该方法用于处理Exception类型的异常
    public HttpResponseEntity exceptionHandle(Exception exception){
        // exceptionHandle方法用于处理异常，并返回一个HttpResponseEntity对象

        return HttpResponseEntity.error(exception.getMessage());
        // 调用HttpResponseEntity类的error方法，将异常信息作为参数传入，返回一个表示错误的HttpResponseEntity对象
    }
}


