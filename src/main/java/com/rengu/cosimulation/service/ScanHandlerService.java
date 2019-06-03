package com.rengu.cosimulation.service;

import com.rengu.cosimulation.entity.DiskScan;
import com.rengu.cosimulation.entity.Order;
import com.rengu.cosimulation.entity.ProcessScan;
import com.rengu.cosimulation.enums.ResultCode;
import com.rengu.cosimulation.exception.ResultException;
import com.rengu.cosimulation.utils.ApplicationConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * Author: XYmar
 * Date: 2019/4/11 11:07
 */
@Slf4j
@Service
public class ScanHandlerService {

    public static final int SCAN_TYPE_CORRECT = 0;
    public static final int SCAN_TYPE_MODIFYED = 1;
    public static final int SCAN_TYPE_UNKNOWN = 2;
    public static final int SCAN_TYPE_MISSING = 3;

    public static final Map<String, List<DiskScan>> DISK_SCAN_RESULT = new ConcurrentHashMap<>();
    public static final Map<String, List<ProcessScan>> PROCESS_SCAN_RESULT = new ConcurrentHashMap<>();

    @Async
    // 扫描设备磁盘处理线程
    public Future<List<DiskScan>> diskScanHandler(Order order) {
        long startTime = System.currentTimeMillis();
        while (true) {
            if (!DeviceService.ONLINE_HOST_ADRESS.containsKey(order.getTargetDevice().getHostAddress())) {
                throw new ResultException(ResultCode.DEVICE_IS_OFFLINE);
            }
            if (System.currentTimeMillis() - startTime >= ApplicationConfig.SCAN_TIME_OUT) {
                throw new ResultException(ResultCode.SCAN_DISK_TIME_OUT);
            }
            if (DISK_SCAN_RESULT.containsKey(order.getId())) {
                return new AsyncResult<>(DISK_SCAN_RESULT.get(order.getId()));
            }
        }
    }

    @Async
    // 扫描设备进程处理线程
    public Future<List<ProcessScan>> processScanHandler(Order order) {
        long startTime = System.currentTimeMillis();
        while (true) {
            if (!DeviceService.ONLINE_HOST_ADRESS.containsKey(order.getTargetDevice().getHostAddress())) {
                throw new ResultException(ResultCode.DEVICE_IS_OFFLINE);
            }
            if (System.currentTimeMillis() - startTime >= ApplicationConfig.SCAN_TIME_OUT) {
                throw new ResultException(ResultCode.SCAN_PROCESS_TIME_OUT);
            }
            if (PROCESS_SCAN_RESULT.containsKey(order.getId())) {
                return new AsyncResult<>(PROCESS_SCAN_RESULT.get(order.getId()));
            }
        }
    }
}