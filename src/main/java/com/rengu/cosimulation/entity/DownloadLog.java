package com.rengu.cosimulation.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import java.util.Date;
import java.util.UUID;

/**
 * Author: XYmar
 * Date: 2019/4/22 9:20
 */
@Entity
@Data
public class DownloadLog {
    @Id
    private String id = UUID.randomUUID().toString();
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime = new Date();
    private String fileName;
    @OneToOne
    private Files files;
    @OneToOne
    private Users users;
}
