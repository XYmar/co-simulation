package com.rengu.cosimulation.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Author: XYmar
 * Date: 2019/4/11 10:26
 */
@Data
public class Heartbeat implements Serializable {

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime = new Date();
    private String hostAddress;
    private String cpuTag;
    private long cpuClock;
    private int cpuUtilization;
    private double ramFreeSize;
    private double ramTotalSize;
    private double downLoadSpeed;
    private double upLoadSpeed;
    private int OSType;
    private String OSName;
    private int count = 3;
}