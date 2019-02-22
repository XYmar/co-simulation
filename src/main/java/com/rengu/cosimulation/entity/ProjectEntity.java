package com.rengu.cosimulation.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.rengu.cosimulation.utils.ApplicationMessage;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * Author: XYmar
 * Date: 2019/2/19 10:06
 */
@Entity
@Data
public class ProjectEntity implements Serializable {
    @Id
    private String id = UUID.randomUUID().toString();
    @NotBlank(message = ApplicationMessage.PROJECT_NAME_NOT_FOUND)
    private String name;
    private String orderNum;      // 令号
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date finishTime = new Date();                 // 节点计划
    @ManyToOne
    private UserEntity pic;                        // 项目负责人
    private boolean deleted = false;               // 项目状态

}
