package com.rengu.cosimulation.entity;

import com.rengu.cosimulation.repository.DesignLinkRepository;
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
public class ProDesignLinkEntity implements Serializable {
    @Id
    private String id = UUID.randomUUID().toString();
    @NotBlank(message = ApplicationMessage.DESIGN_LINK_NAME_NOT_FOUND)
    private String name;                     // 子任务名称
    private String description;              // 子任务描述
    private String finishTime;               // 子任务节点

    @ManyToOne
    private UserEntity userEntity;             // 负责人
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<UserEntity> assessorSet;       // 审核人员

    @ManyToOne
    private DesignLinkEntity designLinkEntity;    //设计环节

    @ManyToOne
    private ProjectEntity projectEntity;        // 所属项目
}
