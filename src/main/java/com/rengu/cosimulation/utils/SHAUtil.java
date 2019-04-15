package com.rengu.cosimulation.utils;

import sun.misc.BASE64Encoder;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;

/**
 * @Author YJH
 * @Date 2019/3/13 13:08
 */
public class SHAUtil {
    /**
     * 获取文件hash值
     * @param fileName
     * @return string
     */
    public static String SHAHashCode(String fileName) {
        BufferedInputStream bis = null;
        try {
            byte[] buffer = new byte[8192];
            int count;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            bis = new BufferedInputStream(new FileInputStream(fileName));
            while ((count = bis.read(buffer)) > 0) {
                digest.update(buffer, 0, count);
            }
            byte[] hash = digest.digest();
            String hashCode = new BASE64Encoder().encode(hash);
            hashCode = byte2Hex(hash);
            return hashCode;
        } catch (Exception e) {

            e.printStackTrace();
            return null;
        } finally {
            try {
                if( bis != null) {
                    bis.close();
                }
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * 将byte转为16进制
     * @param bytes
     * @return
     */
    private static String byte2Hex(byte[] bytes) {
        StringBuffer stringBuffer = new StringBuffer();
        String temp = null;
        for (int i = 0; i < bytes.length; i++) {
            temp = Integer.toHexString(bytes[i] & 0xFF);
            if (temp.length() == 1){
                // 1得到一位的进行补0操作
                stringBuffer.append("0");
            }
            stringBuffer.append(temp);
        }
        return stringBuffer.toString();
    }

}
