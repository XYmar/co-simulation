package com.rengu.cosimulation.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Entity
@Data
public class SubtaskAudit implements Serializable {
    @Id
    private String id = UUID.randomUUID().toString();
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime = new Date();
    private int state;                            // 阶段：:1：校对中  2：审核中  3：会签中  4：批准中
    private String auditDescription;              // 审核意见
    private boolean ifPass;                       // 审核是否通过   0：未通过  1： 通过
    private boolean ifOver;                       // 本阶段的审批是否结束

    @ManyToOne
    private Subtask subtask;
    @ManyToOne
    private Users users;                 // 审核人


   /* private String illustration;  //  审核信息
    private String auditTime;  //  审核时间
    private boolean pass;  //  true：通过 false：驳回
    private int assessState; // 0：校对中   1：审核中  2：会签中  3：批准中

    @ManyToOne
    private Users users;
    @OneToOne
    private Subtask subtask;*/



}
