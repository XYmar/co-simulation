package com.rengu.cosimulation.service;

import com.rengu.cosimulation.entity.*;
import com.rengu.cosimulation.enums.ResultCode;
import com.rengu.cosimulation.exception.ResultException;
import com.rengu.cosimulation.repository.DeviceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.xml.transform.Result;
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
    public static final Map<String, HeartbeatEntity> ONLINE_HOST_ADRESS = new ConcurrentHashMap<>();

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
    public DeviceEntity getDeviceById(String deviceId) {
        if (!hasDeviceById(deviceId)) {
            throw new ResultException(ResultCode.DEVICE_ID_NOT_FOUND);
        }
        return deviceRepository.findById(deviceId).get();
    }

    // 查询所有设备
    public List<DeviceEntity> getDevices() {
        return deviceRepository.findAll();
    }

    // 根据id扫描设备磁盘信息
    public List<ProcessScanResultEntity> getProcessById(String deviceId) throws InterruptedException, ExecutionException, TimeoutException, IOException {
        DeviceEntity deviceEntity = getDeviceById(deviceId);
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setTag(OrderService.PROCESS_SCAN_TAG);
        orderEntity.setTargetDevice(deviceEntity);
        orderService.sendProcessScanOrderByUDP(orderEntity);
        return scanHandlerService.processScanHandler(orderEntity).get(10, TimeUnit.SECONDS);
    }

    // 根据id扫描设备磁盘信息
    public List<DiskScanResultEntity> getDisksById(String deviceId) throws InterruptedException, ExecutionException, TimeoutException, IOException {
        DeviceEntity deviceEntity = getDeviceById(deviceId);
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setTag(OrderService.DISK_SCAN_TAG);
        orderEntity.setTargetDevice(deviceEntity);
        orderService.sendDiskScanOrderByUDP(orderEntity);
        return scanHandlerService.diskScanHandler(orderEntity).get(10, TimeUnit.SECONDS);
    }
}
