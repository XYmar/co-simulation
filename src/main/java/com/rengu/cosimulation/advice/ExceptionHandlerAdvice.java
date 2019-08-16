package com.rengu.cosimulation.advice;

import com.rengu.cosimulation.entity.Result;
import com.rengu.cosimulation.enums.ResultCode;
import com.rengu.cosimulation.exception.ResultException;
import com.rengu.cosimulation.utils.ResultUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Author: XYmar
 * Date: 2019/2/13 14:27
 */
@ControllerAdvice
@ResponseBody
public class ExceptionHandlerAdvice implements ResponseBodyAdvice {
    private ThreadLocal<Object> modelHolder = new ThreadLocal<>();

    private Logger logger = LoggerFactory.getLogger(ExceptionHandlerAdvice.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleIllegalParamException(MethodArgumentNotValidException e) {
        List<ObjectError> errors = e.getBindingResult().getAllErrors();
        String tips = "参数不合法";
        if (errors.size() > 0) {
            tips = errors.get(0).getDefaultMessage();
        }
        return ResultUtils.warn(ResultCode.PARAMETER_ERROR, tips);
    }

    // 权限问题
    @ExceptionHandler(AccessDeniedException.class)
    public Result handleAccessDeniedException(AccessDeniedException e) {
        String tips = e.getMessage();
        return ResultUtils.warn(ResultCode.ACCESS_DENIED_ERROR, tips);
    }

    // 未传递参数异常
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        String tips = "未传递参数";
        return ResultUtils.warn(ResultCode.ARGS_NOT_FOUND_ERROR, tips);
    }

    @ExceptionHandler(ResultException.class)
    public Result handleResultException(ResultException e, HttpServletRequest request) {
        return ResultUtils.warn(e.getResultCode());
    }

    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e, HttpServletRequest request) {
        String tips = e.getMessage();
        return ResultUtils.warn(ResultCode.WEAK_NET_WORK, tips);
    }

    @InitBinder
    public void initBinder(WebDataBinder webDataBinder) {
        // ModelHolder 初始化
        modelHolder.set(webDataBinder.getTarget());
    }

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        // ModelHolder 清理
        modelHolder.remove();
        return body;
    }
}
