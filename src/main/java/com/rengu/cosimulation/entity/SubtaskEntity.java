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
    private int passState;                     // 子任务审核通过与否      0：未通过   1：已通过  2：待审核
    private boolean pass;                      // 当前审核是否通过        true:通过 false:驳回
    private String illustration;               // 审核时的说明
    private int manyCountersignState;          // 多人会签状态            1:未进行   储存一个多人会签状态(5人会签，4人通过 那么当前会签状态为4) 从1开始
    private int countersignState;              // 会签状态               0：未选择   1：无会签  2：一人会签 3：多人会签
    @ManyToOne
    private UserEntity userEntity;             // 负责人
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<UserEntity> collatorSet;       // 核对人
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<UserEntity> auditorSet;        // 审核人
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<UserEntity> countersignSet;    // 会签人
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<UserEntity> apporverSet;       // 批准人
    @ManyToOne
    private DesignLinkEntity designLinkEntity;    //设计环节

    @ManyToOne
    private ProjectEntity projectEntity;        // 所属项目
    @ManyToOne
    private AllIllustrationEntity allIllustration;    // 所有审核的详细信息
//    @OneToMany(mappedBy="subtaskEntity")
//    private Set<ProcessNodeEntity> processNodeEntities;  // 子任务对应的流程图节点

}
