package com.rengu.cosimulation.service;

import com.rengu.cosimulation.entity.Order;
import com.rengu.cosimulation.utils.ApplicationConfig;
import com.rengu.cosimulation.utils.FormatUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.*;

/**
 * Author: XYmar
 * Date: 2019/4/11 10:44
 */
@Slf4j
@Service
public class OrderService {

    public static final String DEPLOY_DESIGN_SCAN = "S102";
    public static final String DEPLOY_DESIGN_SCAN_WITH_EXTENSIONS = "S103";
    public static final String PROCESS_SCAN_TAG = "S105";
    public static final String DISK_SCAN_TAG = "S106";
    // 客户端返回报问表示
    public static final String DEPLOY_DESIGN_SCAN_RESULT_TAG = "C102";
    public static final String PROCESS_SCAN_RESULT_TAG = "C105";
    public static final String DISK_SCAN_RESULT_TAG = "C106";

    public void sendProcessScanOrderByUDP(Order order) throws IOException {
        String tag = FormatUtils.getString(order.getTag(), 4);
        String type = FormatUtils.getString("", 1);
        String uuid = FormatUtils.getString(order.getId(), 37);
        sandMessageByUDP(order.getTargetDevice().getHostAddress(), tag + type + uuid);
    }

    public void sendDiskScanOrderByUDP(Order order) throws IOException {
        String tag = FormatUtils.getString(order.getTag(), 4);
        String type = FormatUtils.getString("", 1);
        String uuid = FormatUtils.getString(order.getId(), 37);
        sandMessageByUDP(order.getTargetDevice().getHostAddress(), tag + type + uuid);
    }

    private void sandMessageByUDP(String hostAdress, String message) throws IOException {
        DatagramSocket datagramSocket = new DatagramSocket();
        InetAddress inetAddress = InetAddress.getByName(hostAdress);
        SocketAddress socketAddress = new InetSocketAddress(inetAddress, ApplicationConfig.UDP_SEND_PORT);
        DatagramPacket datagramPacket = new DatagramPacket(message.getBytes(), message.length(), socketAddress);
        datagramSocket.send(datagramPacket);
        datagramSocket.close();
    }
}
