package com.rengu.cosimulation.utils;

import com.rengu.cosimulation.entity.Result;
import com.rengu.cosimulation.enums.ResultCode;

/**
 * Author: XYmar
 * Date: 2019/2/13 15:03
 */
public class ResultUtils {
    public static Result success(Object data) {
        return new Result<>(ResultCode.SUCCESS, data);
    }

    public static Result warn(ResultCode resultCode, String msg) {
        Result<Object> result = new Result<>(resultCode);
        result.setMsg(msg);
        return result;
    }

    public static Result warn(ResultCode resultCode) {
        return new Result(resultCode);
    }
}
