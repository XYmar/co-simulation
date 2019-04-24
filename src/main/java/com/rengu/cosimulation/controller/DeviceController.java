package com.rengu.cosimulation.controller;

import com.rengu.cosimulation.entity.ResultEntity;
import com.rengu.cosimulation.service.DeviceService;
import com.rengu.cosimulation.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Author: XYmar
 * Date: 2019/4/11 11:39
 */
@RestController
@RequestMapping(value = "/devices")
public class DeviceController {
    private final DeviceService deviceService;

    @Autowired
    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    // 根据Id查询设备
    @GetMapping(value = "/{deviceId}")
    public ResultEntity getDeviceById(@PathVariable(value = "deviceId") String deviceId) {
        return ResultUtils.success(deviceService.getDeviceById(deviceId));
    }

    // 查询所有设备
    @GetMapping
    public ResultEntity getDevices() {
        return ResultUtils.success(deviceService.getDevices());
    }

    // 获取进程信息
    @GetMapping(value = "/{deviceId}/process")
    public ResultEntity getProcessById(@PathVariable(value = "deviceId") String deviceId) throws InterruptedException, ExecutionException, TimeoutException, IOException {
        return ResultUtils.success(deviceService.getProcessById(deviceId));
    }

    // 获取磁盘信息
    @GetMapping(value = "/{deviceId}/disks")
    public ResultEntity getDisksById(@PathVariable(value = "deviceId") String deviceId) throws InterruptedException, ExecutionException, TimeoutException, IOException {
        return ResultUtils.success(deviceService.getDisksById(deviceId));
    }
}
