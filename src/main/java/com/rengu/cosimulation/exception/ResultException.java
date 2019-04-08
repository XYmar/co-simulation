package com.rengu.cosimulation.exception;

import com.rengu.cosimulation.enums.ResultCode;

/**
 * Author: XYmar
 * Date: 2019/2/13 13:03
 * 结果异常，会被 ExceptionHandler 捕捉并返回给前端
 */
public class ResultException extends RuntimeException{
    private ResultCode resultCode;

    public ResultException(ResultCode resultCode) {
        super(resultCode.getMsg());
        this.resultCode = resultCode;
    }

    public ResultCode getResultCode() {
        return resultCode;
    }
}
