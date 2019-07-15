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
public class Subtask implements Serializable {
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
    private boolean ifModifyApprove;           // 二次修改申请是否通过
    private String version;
    private int manyCounterSignState;          // 多人会签时，文件状态(几人已会签过)
    private int auditMode;                     // 会签模式               0：未选择   1：无会签  2：一人会签 3：多人会签
    @ManyToOne
    @JoinColumn(name = "users_id")
    private Users users;             // 负责人
    @ManyToMany
    private Set<Users> proofSet;  // 校对人
    @ManyToMany
    private Set<Users> auditSet;  // 审核人
    @ManyToMany
    private Set<Users> countSet;  // 会签人
    @ManyToMany
    private Set<Users> approveSet;  // 批准人
    @ManyToOne
    private DesignLink designLink;    //设计环节

    @ManyToOne
    private Project project;        // 所属项目
//    @OneToMany(mappedBy="subtask")
//    private Set<ProcessNode0> processNodeEntities;  // 子任务对应的流程图节点

}
