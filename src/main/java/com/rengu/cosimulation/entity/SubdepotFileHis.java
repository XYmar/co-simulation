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
 * Date: 2019/4/9 10:30
 * 子库文件历史、临时文件实体
 */
@Entity
@Data
public class SubdepotFileHis implements Serializable {
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

    private int state;                    // 状态：:1：校对中  2：审核中  3：会签中  4：批准中   5：审批结束  6：申请二次修改中  7： 二次修改中
    private boolean ifReject;             // 是否被驳回
    private int manyCounterSignState;      // 多人会签时，文件状态(几人已会签过)
    private boolean ifApprove;             // 审核是否通过
    private int auditMode;                     // 模式： 1：无会签  2：一人会签  3：多人会签
    private boolean ifModifyApprove;       // 二次修改申请是否通过
    private boolean ifDirectModify;        // 是否为直接修改
    boolean ifTemp;                          // 是否是临时文件
    private int rejectState;               // 记录被驳回的流程

    @ManyToOne
    private Files files;
    @ManyToOne
    @JoinColumn(name = "subibraryEntity_id")
    private SubDepot subDepot;
    @ManyToOne
    @JoinColumn(name = "subDepotFile_id")
    private SubDepotFile leastSubDepotFile;                 // 历史文件对应的子库文件
    @ManyToMany
    private Set<Users> proofSet;  // 校对人
    @ManyToMany
    private Set<Users> auditSet;  // 审核人
    @ManyToMany
    private Set<Users> countSet;  // 会签人
    @ManyToMany
    private Set<Users> approveSet;  // 批准人
    @ManyToOne
    @JoinColumn(name = "users_id")
    private Users users;
}