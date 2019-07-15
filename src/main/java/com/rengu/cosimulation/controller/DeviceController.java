package com.rengu.cosimulation.controller;

import com.rengu.cosimulation.entity.Device;
import com.rengu.cosimulation.entity.Result;
import com.rengu.cosimulation.service.DeviceService;
import com.rengu.cosimulation.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    // 创建设备 预订
    @PostMapping
    public Result saveDeviceByProject(Device device, String userId, String subtaskId) {
        return ResultUtils.success(deviceService.saveDeviceByProject(device, userId, subtaskId));
    }

    // 根据Id查询设备
    @GetMapping(value = "/{deviceId}")
    public Result getDeviceById(@PathVariable(value = "deviceId") String deviceId) {
        return ResultUtils.success(deviceService.getDeviceById(deviceId));
    }

    // 查询所有设备
    @GetMapping
    public Result getDevices() {
        return ResultUtils.success(deviceService.getDevices());
    }

    // 根据Id清除设备
    @DeleteMapping(value = "/{deviceId}/clean")
    public Result cleanDeviceById(@PathVariable(value = "deviceId") String deviceId) {
        return ResultUtils.success(deviceService.cleanDeviceById(deviceId));
    }

    // 获取进程信息
    @GetMapping(value = "/{deviceId}/process")
    public Result getProcessById(@PathVariable(value = "deviceId") String deviceId) throws InterruptedException, ExecutionException, TimeoutException, IOException {
        return ResultUtils.success(deviceService.getProcessById(deviceId));
    }

    // 获取磁盘信息
    @GetMapping(value = "/{deviceId}/disks")
    public Result getDisksById(@PathVariable(value = "deviceId") String deviceId) throws InterruptedException, ExecutionException, TimeoutException, IOException {
        return ResultUtils.success(deviceService.getDisksById(deviceId));
    }

}
