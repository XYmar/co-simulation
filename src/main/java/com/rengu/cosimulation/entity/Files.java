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
 * Date: 2019/2/28 10:37
 * 文件实体类
 */
@Entity
@Data
public class Files implements Serializable {
    @Id
    private String id = UUID.randomUUID().toString();
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime = new Date();
    private String mD5;
    private String postfix;               // 后缀
    private String type;                  // 类型：  0：参数文件  1：模型文件  2：报告文件
    private int secretClass;              // 密级：  0：公开  1：内部  2：秘密  3：机密  4：绝密
    private long fileSize;
    private String localPath;
}
