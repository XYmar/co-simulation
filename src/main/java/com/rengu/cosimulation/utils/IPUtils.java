package com.rengu.cosimulation.utils;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-23 12:51
 **/

@Slf4j
public class IPUtils {

    /**
     * 判断是否为ipv4地址
     */
    public static boolean isIPv4Address(String IPv4Address) {
        String lower = "(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])";
        String regex = lower + "(\\." + lower + "){3}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(IPv4Address);
        return matcher.matches();
    }


    /**
     * ip地址转成long型数字
     * 将IP地址转化成整数的方法如下：
     * 1、通过String的split方法按.分隔得到4个长度的数组
     * 2、通过左移位操作（<<）给每一段的数字加权，第一段的权为2的24次方，第二段的权为2的16次方，第三段的权为2的8次方，最后一段的权为1
     *
     * @param hostAddress
     * @return
     */
    public static long ipToLong(String hostAddress) {
        String[] ip = hostAddress.split("\\.");
        return (Long.parseLong(ip[0]) << 24) + (Long.parseLong(ip[1]) << 16) + (Long.parseLong(ip[2]) << 8) + Long.parseLong(ip[3]);
    }

    /**
     * 将十进制整数形式转换成127.0.0.1形式的ip地址
     * 将整数形式的IP地址转化成字符串的方法如下：
     * 1、将整数值进行右移位操作（>>>），右移24位，右移时高位补0，得到的数字即为第一段IP。
     * 2、通过与操作符（&）将整数值的高8位设为0，再右移16位，得到的数字即为第二段IP。
     * 3、通过与操作符吧整数值的高16位设为0，再右移8位，得到的数字即为第三段IP。
     * 4、通过与操作符吧整数值的高24位设为0，得到的数字即为第四段IP。
     *
     * @param hostAddress
     * @return
     */
    public static String longToIP(long hostAddress) {
        StringBuffer sb = new StringBuffer();
        // 直接右移24位
        sb.append((hostAddress >>> 24));
        sb.append(".");
        // 将高8位置0，然后右移16位
        sb.append(((hostAddress & 0x00FFFFFF) >>> 16));
        sb.append(".");
        // 将高16位置0，然后右移8位
        sb.append(((hostAddress & 0x0000FFFF) >>> 8));
        sb.append(".");
        // 将高24位置0
        sb.append((hostAddress & 0x000000FF));
        return sb.toString();
    }

    public static List<InterfaceAddress> getLocalIPs() throws SocketException {
        List<InterfaceAddress> interfaceAddressList = new ArrayList<>();
        Enumeration<NetworkInterface> networkInterfaceEnumeration = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaceEnumeration.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaceEnumeration.nextElement();
            if (!networkInterface.isLoopback() && !networkInterface.isVirtual() && networkInterface.isUp() && networkInterface.supportsMulticast()) {
                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    if (interfaceAddress.getAddress() != null && interfaceAddress.getBroadcast() != null) {
                        InetAddress inetAddress = interfaceAddress.getAddress();
                        if (!inetAddress.isLoopbackAddress() && IPUtils.isIPv4Address(inetAddress.getHostAddress())) {
                            interfaceAddressList.add(interfaceAddress);
                        }
                    }
                }
            }
        }
        return interfaceAddressList;
    }
}
