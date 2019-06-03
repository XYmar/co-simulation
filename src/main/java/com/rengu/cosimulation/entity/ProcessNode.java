package com.rengu.cosimulation.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Author: XYmar
 * Date: 2019/4/17 18:50
 * 流程节点实体类
 */
@Entity
@Data
public class ProcessNode implements Serializable {
    @Id
    private String id = UUID.randomUUID().toString();
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime = new Date();
    @NotBlank(message = "节点名称不能为空")
    private String nodeName;                           // 节点名称
    @NotBlank(message = "节点标识不存在")
    private String selfSign;                           // 节点标识
    @NotBlank(message = "节点位置信息不能为空")
    private String location;                           // 节点位置信息（x,y）
    @NotNull (message = "节点类型不能为空")
    private String figure;                             // 节点类型（其中subtask表示子任务）
    @NotBlank(message = "节点大小不能为空")
    private String nodeSize;                           // 节点框的大小

    @ManyToOne
    private Project project;               // 节点所属项目

    @OneToOne(cascade= CascadeType.ALL)
    @JoinColumn(name = "Subtask_id")//设置在employee表中的关联字段(外键)
    private Subtask subtask;
    @OneToMany(cascade= CascadeType.ALL)
    private List<Link> linkList;            // 节点对应的流程连接
}
