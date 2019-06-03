package com.rengu.cosimulation.controller;

import com.rengu.cosimulation.entity.Result;
import com.rengu.cosimulation.service.DownloadLogsService;
import com.rengu.cosimulation.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Author: XYmar
 * Date: 2019/5/5 13:27
 */
@RestController
@RequestMapping("/downLogs")
public class DownloadLogsController {
    private final DownloadLogsService downloadLogsService;

    @Autowired
    public DownloadLogsController(DownloadLogsService downloadLogsService) {
        this.downloadLogsService = downloadLogsService;
    }

    @GetMapping
    public Result getLogs(){
        return ResultUtils.success(downloadLogsService.getLogs());
    }
}
