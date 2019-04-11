package com.rengu.cosimulation.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * Author: XYmar
 * Date: 2019/3/27 16:02
 */
@Data
@Entity
public class DeviceEntity implements Serializable {
    @Id
    private String id = UUID.randomUUID().toString();
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime = new Date();
    private String name;
    private String hostAddress;
    private String description;
    private String deployPath;
    private boolean deleted = false;
}