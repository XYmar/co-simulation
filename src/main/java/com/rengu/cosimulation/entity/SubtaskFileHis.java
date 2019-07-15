package com.rengu.cosimulation.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

/**
 * Author: XYmar
 * Date: 2019/4/12 20:27
 */
@Entity
@Data
public class SubtaskFileHis implements Serializable {
    @Id
    private String id = UUID.randomUUID().toString();
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime = new Date();
    private String name;
    private String postfix;               // 后缀
    private String type;                  // 类型：  0：参数文件  1：模型文件  2：报告文件  3：实验数据
    private int secretClass;              // 密级：  0：公开  1：内部  2：秘密  3：机密  4：绝密
    private String productNo;                // 产品型号
    private String fileNo;                   // 文件图号
    private String version;               // 版本（根据提交次数累加，只有修改才变更版本）
    private boolean ifDirectModify;        // 是否为直接修改
    boolean ifTemp;                          // 是否是临时文件

    @ManyToOne
    private Files files;
    @ManyToOne
    private Subtask subTask;
    @ManyToOne
    @JoinColumn(name = "subtask_id")
    private SubtaskFile leastSubtaskFile;                 // 历史文件对应的子任务
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<SubDepot> subDepotSet;
}