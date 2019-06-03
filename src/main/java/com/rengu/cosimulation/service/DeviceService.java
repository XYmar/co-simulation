package com.rengu.cosimulation.service;

import com.rengu.cosimulation.entity.*;
import com.rengu.cosimulation.enums.ResultCode;
import com.rengu.cosimulation.exception.ResultException;
import com.rengu.cosimulation.repository.DeviceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public DeviceService(DeviceRepository deviceRepository, OrderService orderService, ScanHandlerService scanHandlerService) {
        this.deviceRepository = deviceRepository;
        this.orderService = orderService;
        this.scanHandlerService = scanHandlerService;
    }

    // 根据Id判断设备是否存在
    public boolean hasDeviceById(String deviceId) {
        if (StringUtils.isEmpty(deviceId)) {
            return false;
        }
        return deviceRepository.existsById(deviceId);
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
