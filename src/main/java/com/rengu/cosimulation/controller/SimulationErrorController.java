package com.rengu.cosimulation.controller;

import com.rengu.cosimulation.entity.ResultEntity;
import com.rengu.cosimulation.enums.ResultCode;
import com.rengu.cosimulation.utils.ResultUtils;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Author: XYmar
 * Date: 2019/2/13 15:43
 */
@Controller
@RequestMapping("${server.error.path:${error.path:/error}}")
public class SimulationErrorController implements ErrorController {
    @Override
    public String getErrorPath() {
        return "/error";
    }

    @RequestMapping
    @ResponseBody
    public ResultEntity doHandleError() {
        return ResultUtils.warn(ResultCode.WEAK_NET_WORK);
    }
}
