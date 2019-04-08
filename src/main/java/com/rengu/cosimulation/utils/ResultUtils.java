package com.rengu.cosimulation.utils;

import com.rengu.cosimulation.entity.ResultEntity;
import com.rengu.cosimulation.enums.ResultCode;

/**
 * Author: XYmar
 * Date: 2019/2/13 15:03
 */
public class ResultUtils {
    public static ResultEntity success(Object data) {
        return new ResultEntity<>(ResultCode.SUCCESS, data);
    }

    public static ResultEntity warn(ResultCode resultCode, String msg) {
        ResultEntity<Object> resultEntity = new ResultEntity<>(resultCode);
        resultEntity.setMsg(msg);
        return resultEntity;
    }

    public static ResultEntity warn(ResultCode resultCode) {
        return new ResultEntity(resultCode);
    }
}
