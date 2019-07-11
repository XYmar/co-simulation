package com.rengu.cosimulation.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.*;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-27 15:24
 **/

@Slf4j
public class ServerCastUtils {

    private static void sendMessageByBroadcast(InterfaceAddress interfaceAddress, String message) throws IOException {
        DatagramSocket datagramSocket = new DatagramSocket();
        SocketAddress socketAddress = new InetSocketAddress(interfaceAddress.getBroadcast(), ApplicationConfig.SERVER_BROAD_CAST_PORT);
        DatagramPacket datagramPacket = new DatagramPacket(message.getBytes(), message.length(), socketAddress);
        datagramSocket.send(datagramPacket);
        datagramSocket.close();
    }

    // 组播发送服务器IP
    private static void sendMessageByMulticast(InterfaceAddress interfaceAddress, String message) throws IOException {
        InetAddress inetAddress = InetAddress.getByName(ApplicationConfig.SERVER_CAST_ADDRESS);
        MulticastSocket multicastSocket = new MulticastSocket(new InetSocketAddress(interfaceAddress.getAddress(), ApplicationConfig.SERVER_MULTI_CAST_PORT));
        multicastSocket.setLoopbackMode(true);
        multicastSocket.joinGroup(inetAddress);
        DatagramPacket datagramPacket = new DatagramPacket(message.getBytes(), message.length(), inetAddress, ApplicationConfig.SERVER_MULTI_CAST_PORT);
        multicastSocket.send(datagramPacket);
        multicastSocket.leaveGroup(inetAddress);
        multicastSocket.close();
    }

    public static void sendMessage(InterfaceAddress interfaceAddress) throws IOException {
        String ipAddress = interfaceAddress.getAddress().toString().replace("/", "");
        String message = ("S101" + ipAddress);
        sendMessageByMulticast(interfaceAddress, message);
    }
}
