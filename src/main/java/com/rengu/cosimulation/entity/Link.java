package com.rengu.cosimulation.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * Author: XYmar
 * Date: 2019/4/17 18:51
 * 流程节点连接关系实体类
 */
@Entity
@Data
public class Link implements Serializable {
    @Id
    private String id = UUID.randomUUID().toString();
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime = new Date();
    @NotBlank(message = "请传入父节点")
    private String parentId;                           // 父节点id
    @NotBlank(message = "请传入本节点")
    private String selfId;                             // 本节点id
    @NotNull(message = "节点出发方向不能为空")
    private String fromPort;                           // 节点从父节点哪个方向出发
    @NotNull (message = "节点到达方向不能为空")
    private String toPort;                             // 节点从哪个方向连接到本身
    @ManyToOne
    private Project project;               // 节点所属项目
}
