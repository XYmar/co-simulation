package com.rengu.cosimulation.service;

import com.rengu.cosimulation.entity.*;
import com.rengu.cosimulation.enums.ResultCode;
import com.rengu.cosimulation.exception.ResultException;
import com.rengu.cosimulation.repository.DeviceRepository;
import com.rengu.cosimulation.utils.ApplicationConfig;
import com.rengu.cosimulation.utils.FormatUtils;
import com.rengu.cosimulation.utils.IPUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Author: XYmar
 * Date: 2019/3/27 16:06
 */
@Service
@Slf4j
@Transactional
public class DeviceService {
    public static final Map<String, Heartbeat> ONLINE_HOST_ADRESS = new ConcurrentHashMap<>();

    private final DeviceRepository deviceRepository;
    private final OrderService orderService;
    private final ScanHandlerService scanHandlerService;
    private final UserService userService;
    private final SubtaskService subtaskService;

    @Autowired
    public DeviceService(DeviceRepository deviceRepository, OrderService orderService, ScanHandlerService scanHandlerService, UserService userService, SubtaskService subtaskService) {
        this.deviceRepository = deviceRepository;
        this.orderService = orderService;
        this.scanHandlerService = scanHandlerService;
        this.userService = userService;
        this.subtaskService = subtaskService;
    }

    // 根据Id判断设备是否存在
    public boolean hasDeviceById(String deviceId) {
        if (StringUtils.isEmpty(deviceId)) {
            return false;
        }
        return deviceRepository.existsById(deviceId);
    }

    // 创建设备 预订
    @CachePut(value = "Device_Cache", key = "#device.id")
    public Device saveDeviceByProject(Device device, String userId, String subtaskId) {
        if (StringUtils.isEmpty(device.getName())) {
            throw new ResultException(ResultCode.DEVICE_NAME_ARGS_NOT_FOUND);
        }
        if (StringUtils.isEmpty(device.getHostAddress()) || !IPUtils.isIPv4Address(device.getHostAddress())) {
            throw new ResultException(ResultCode.DEVICE_HOST_ADDRESS_ARGS_NOT_FOUND);
        }
        if (hasDeviceByHostAddressAndIfDeleted(device.getHostAddress(), false)) {
            throw new ResultException(ResultCode.DEVICE_HOST_ADDRESS_EXISTED);
        }
        if(StringUtils.isEmpty(device.getInterval())){
            throw new ResultException(ResultCode.DEVICE_INTERVAL_NOT_FOUND);
        }
        Subtask subtask = subtaskService.getSubtaskById(subtaskId);
        if(!subtask.getUsers().getId().equals(userId)){
            throw new ResultException(ResultCode.SUBTASK_DEVICE_ARRANGE_AUTHORITY_DENIED_ERROR);
        }
        if(subtask.getState() < ApplicationConfig.SUBTASK_START || subtask.getState() > ApplicationConfig.SUBTASK_APPROVE){
            throw new ResultException(ResultCode.SUBTASK_DEVICE_ARRANGE_DENIED_ERROR);
        }
        Users user = userService.getUserById(userId);
        device.setUsers(user);
        device.setSubtask(subtask);
        // device.setDeployPath(FormatUtils.formatPath(device.getDeployPath()));
        return deviceRepository.save(device);
    }

    // 根据IP、是否删除查询设备是否存在
    public boolean hasDeviceByHostAddressAndIfDeleted(String hostAddress, boolean deleted) {
        if (StringUtils.isEmpty(hostAddress) || !IPUtils.isIPv4Address(hostAddress)) {
            return false;
        }
        return deviceRepository.existsByHostAddressAndIfDeleted(hostAddress, deleted);
    }

    // 根据Id查询设备
    @Cacheable(value = "Device_Cache", key = "#deviceId")
    public Device getDeviceById(String deviceId) {
        if (!hasDeviceById(deviceId)) {
            throw new ResultException(ResultCode.DEVICE_ID_NOT_FOUND);
        }
        return deviceRepository.findById(deviceId).get();
    }

    // 查询所有设备
    public List<Device> getDevices() {
        return deviceRepository.findAll();
    }

    // 根据Id清除设备
    @CacheEvict(value = "Device_Cache", key = "#deviceId")
    public Device cleanDeviceById(String deviceId) {
        Device deviceEntity = getDeviceById(deviceId);
        deviceRepository.delete(deviceEntity);
        return deviceEntity;
    }

    // 根据id扫描设备磁盘信息
    public List<ProcessScan> getProcessById(String deviceId) throws InterruptedException, ExecutionException, TimeoutException, IOException {
        Device device = getDeviceById(deviceId);
        Order order = new Order();
        order.setTag(OrderService.PROCESS_SCAN_TAG);
        order.setTargetDevice(device);
        orderService.sendProcessScanOrderByUDP(order);
        return scanHandlerService.processScanHandler(order).get(10, TimeUnit.SECONDS);
    }

    // 根据id扫描设备磁盘信息
    public List<DiskScan> getDisksById(String deviceId) throws InterruptedException, ExecutionException, TimeoutException, IOException {
        Device device = getDeviceById(deviceId);
        Order order = new Order();
        order.setTag(OrderService.DISK_SCAN_TAG);
        order.setTargetDevice(device);
        orderService.sendDiskScanOrderByUDP(order);
        return scanHandlerService.diskScanHandler(order).get(10, TimeUnit.SECONDS);
    }
}
