package com.rengu.cosimulation.entity;


import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Data
public class AllIllustrationEntity implements Serializable {
    @Id
    private String id = UUID.randomUUID().toString();
    private String illustration;  //  审核信息
    private String auditTime;  //  审核时间
    private boolean pass;  //  true：通过 false：驳回
    private int assessState; // 0：校对中   1：审核中  2：会签中  3：批准中

    @ManyToOne
    private UserEntity userEntity;
    @OneToOne
    private SubtaskEntity subtaskEntity;
}
