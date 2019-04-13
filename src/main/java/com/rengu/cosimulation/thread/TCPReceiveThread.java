package com.rengu.cosimulation.thread;

import com.rengu.cosimulation.entity.DiskScanResultEntity;
import com.rengu.cosimulation.entity.ProcessScanResultEntity;
import com.rengu.cosimulation.service.OrderService;
import com.rengu.cosimulation.service.ScanHandlerService;
import com.rengu.cosimulation.utils.ApplicationConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: XYmar
 * Date: 2019/4/11 10:32
 */
@Slf4j
@Component
public class TCPReceiveThread {

    // TCP报文接受进程
    @Async
    public void TCPMessageReceiver() {
        try {
            log.info("COSIMULATION服务器-启动客户端TCP报文监听线程，监听端口：" + ApplicationConfig.TCP_RECEIVE_PORT);
            ServerSocket serverSocket = new ServerSocket(ApplicationConfig.TCP_RECEIVE_PORT);
            while (true) {
                socketHandler(serverSocket.accept());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Async
    public void socketHandler(Socket socket) throws IOException {
        try {
            InputStream inputStream = socket.getInputStream();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            IOUtils.copy(inputStream, byteArrayOutputStream);
            bytesHandler(byteArrayOutputStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socket.shutdownOutput();
            socket.close();
        }
    }

    private void bytesHandler(byte[] bytes) {
        int pointer = 0;
        String messageType = new String(bytes, 0, 4).trim();
        pointer = pointer + 4;
        // 进程扫描信息
        if (messageType.equals(OrderService.PROCESS_SCAN_RESULT_TAG)) {
            String id = new String(bytes, pointer, 37).trim();
            pointer = pointer + 37;
            List<ProcessScanResultEntity> processScanResultEntityList = new ArrayList<>();
            while (pointer + 5 + 128 + 8 + 8 < bytes.length) {
                try {
                    String pid = new String(bytes, pointer, 5).trim();
                    pointer = pointer + 5;
                    String name = new String(bytes, pointer, 128).trim();
                    pointer = pointer + 128;
                    String priority = new String(bytes, pointer, 8).trim();
                    pointer = pointer + 8;
                    double ramUsedSize = Double.parseDouble(new String(bytes, pointer, 8).trim()) / 1024;
                    pointer = pointer + 8;
                    ProcessScanResultEntity processScanResultEntity = new ProcessScanResultEntity();
                    processScanResultEntity.setPid(pid);
                    processScanResultEntity.setName(name);
                    processScanResultEntity.setRamUsedSize(ramUsedSize);
                    processScanResultEntityList.add(processScanResultEntity);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            ScanHandlerService.PROCESS_SCAN_RESULT.put(id, processScanResultEntityList);
        }
        // 扫描磁盘结果解析
        if (messageType.equals(OrderService.DISK_SCAN_RESULT_TAG)) {
            String id = new String(bytes, pointer, 37).trim();
            pointer = pointer + 37;
            List<DiskScanResultEntity> diskScanResultEntityList = new ArrayList<>();
            while (pointer + 32 + 12 + 12 <= bytes.length) {
                try {
                    String name = new String(bytes, pointer, 32).trim().replace("\\", "/");
                    pointer = pointer + 32;
                    double size = Double.parseDouble(new String(bytes, pointer, 12).trim());
                    pointer = pointer + 12;
                    double usedSize = Double.parseDouble(new String(bytes, pointer, 12).trim());
                    pointer = pointer + 12;
                    DiskScanResultEntity diskScanResultEntity = new DiskScanResultEntity();
                    diskScanResultEntity.setName(name);
                    diskScanResultEntity.setSize(size);
                    diskScanResultEntity.setUsedSize(usedSize);
                    diskScanResultEntityList.add(diskScanResultEntity);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            ScanHandlerService.DISK_SCAN_RESULT.put(id, diskScanResultEntityList);
        }
    }
}
