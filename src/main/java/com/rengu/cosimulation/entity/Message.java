package com.rengu.cosimulation.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * Author: XYmar
 * Date: 2019/4/22 15:52
 */
@Entity
@Data
public class Message implements Serializable {
    @Id
    private String id = UUID.randomUUID().toString();
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime = new Date();
    private String mainOperatorName;             // 主要操作人
    private int messageOperate;                  // 操作
    private int mainBody;                        // 操作主体
    private String description;                  // 操作描述
    private boolean ifRead;                   // 操作是否与被操作人有关
    private String arrangedPersonName;           // 被操作人
}
