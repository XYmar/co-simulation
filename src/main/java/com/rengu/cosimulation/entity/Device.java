package com.rengu.cosimulation.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * Author: XYmar
 * Date: 2019/3/27 16:02
 */
@Data
@Entity
public class Device implements Serializable {
    @Id
    private String id = UUID.randomUUID().toString();
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime = new Date();
    private String name;
    private String hostAddress;
    private String description;
    private double interval;                       // 预订时间
    // private String deployPath;
    @Column(name = "ifDeleted")
    @Type(type="yes_no")
    private boolean ifDeleted = false;
    @OneToOne
    private Users users;                            // 预订用户
    @OneToOne
    private Subtask subtask;
}