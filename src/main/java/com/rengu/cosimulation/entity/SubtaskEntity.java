package com.rengu.cosimulation.entity;

import com.rengu.cosimulation.utils.ApplicationMessage;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

/**
 * Author: XYmar
 * Date: 2019/2/26 10:01
 */
@Entity
@Data
public class SubtaskEntity implements Serializable {
    @Id
    private String id = UUID.randomUUID().toString();
    @NotBlank(message = ApplicationMessage.DESIGN_LINK_NAME_NOT_FOUND)
    private String name;                       // 子任务名称
    private String description;                // 子任务描述
    private String finishTime;                 // 子任务节点
    private int state;                         // 子任务的执行状态       0：未进行   1：进行中   2：校对中   3：审核中  4：会签中  5：批准中  6：审核完成
    private boolean ifApprove;                 // 审核是否通过
    private boolean ifReject;                  // 是否被驳回
    private int rejectState;                   // 记录被驳回的流程
    private int manyCounterSignState;          // 多人会签时，文件状态(几人已会签过)
    private int auditMode;                     // 会签模式               0：未选择   1：无会签  2：一人会签 3：多人会签
    @ManyToOne
    @JoinColumn(name = "userEntity_id")
    private UserEntity userEntity;             // 负责人
    @ManyToMany
    private Set<UserEntity> proofreadUserSet;  // 校对人
    @ManyToMany
    private Set<UserEntity> auditUserSet;  // 审核人
    @ManyToMany
    private Set<UserEntity> countersignUserSet;  // 会签人
    @ManyToMany
    private Set<UserEntity> approveUserSet;  // 批准人
    /*@ManyToMany(fetch = FetchType.EAGER)
    private Set<UserEntity> collatorSet;       // 核对人
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<UserEntity> auditorSet;        // 审核人
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<UserEntity> countersignSet;    // 会签人
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<UserEntity> apporverSet;       // 批准人*/
    @ManyToOne
    private DesignLinkEntity designLinkEntity;    //设计环节

    @ManyToOne
    private ProjectEntity projectEntity;        // 所属项目
//    @OneToMany(mappedBy="subtaskEntity")
//    private Set<ProcessNodeEntity> processNodeEntities;  // 子任务对应的流程图节点

}
