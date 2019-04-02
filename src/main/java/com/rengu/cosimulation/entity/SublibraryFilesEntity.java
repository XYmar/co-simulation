package com.rengu.cosimulation.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

/**
 * Author: XYmar
 * Date: 2019/4/1 9:37
 */
@Entity
@Data
public class SublibraryFilesEntity {
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

    private int state;                    // 状态：:1：校对中  2：审核中  3：会签中  4：批准中
    private boolean ifApprove;             // 审核是否通过
    private int mode;                     // 模式： 1：无会签  2：一人会签  3：多人会签

    @ManyToOne
    private FileEntity fileEntity;
    @ManyToOne
    private SublibraryEntity sublibraryEntity;
    @OneToMany
    private Set<UserEntity> proofreadUserSet;  // 校对人
    @OneToMany
    private Set<UserEntity> auditUserSet;  // 审核人
    @OneToMany
    private Set<UserEntity> countersignUserSet;  // 会签人
    @OneToMany
    private Set<UserEntity> approveUserSet;  // 批准人
    @ManyToOne
    private UserEntity userEntity;

}
