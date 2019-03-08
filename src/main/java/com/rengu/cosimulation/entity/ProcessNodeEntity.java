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
 * Date: 2019/3/7 12:55
 * 流程节点实体类
 */
@Entity
@Data
public class ProcessNodeEntity implements Serializable {
    @Id
    private String id = UUID.randomUUID().toString();
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime = new Date();
    @NotBlank(message = "节点名称不能为空")
    private String nodeName;                           // 节点名称
    @NotBlank(message = "节点标识不存在")
    private String sign;                                  // 节点标识
    @NotNull (message = "父节点标识不存在")
    private String parentSign;                            // 父节点标识
    @NotBlank(message = "节点位置信息不能为空")
    private String location;                           // 节点位置信息（x,y）
    @NotNull (message = "节点类型不能为空")
    private String figure;                             // 节点类型（其中subtask表示子任务）
    @NotNull(message = "节点出发方向不能为空")
    private String fromPort;                           // 节点从父节点哪个方向出发
    @NotNull (message = "节点到达方向不能为空")
    private String toPort;                             // 节点从哪个方向连接到本身
    @NotBlank(message = "节点大小不能为空")
    private String nodeSize;                               // 节点框的大小

    @ManyToOne
    private ProjectEntity projectEntity;               // 节点所属项目
}
