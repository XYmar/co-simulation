package com.rengu.cosimulation.entity;

import com.rengu.cosimulation.utils.ApplicationMessage;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.UUID;

/**
 * Author: XYmar
 * Date: 2019/2/19 16:49
 * 结构建模、电气建模、结构仿真、电器仿真、热学仿真、力学仿真、装配仿真等。
 */
@Entity
@Data
public class DesignLink implements Serializable {
    @Id
    private String id = UUID.randomUUID().toString();
    @NotBlank(message = ApplicationMessage.DESIGN_LINK_NAME_NOT_FOUND)
    private String name;                  // 设计环节名称
    private String description;           // 设计环节描述
    private int type;                     // 类型   0：建模   1：仿真

}
