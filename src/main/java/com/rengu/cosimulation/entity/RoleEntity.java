package com.rengu.cosimulation.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.rengu.cosimulation.utils.ApplicationMessage;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotEmpty;
import java.util.Date;
import java.util.UUID;

/**
 * Author: XYmar
 * Date: 2019/2/12 15:54
 */
@Entity
@Data
public class RoleEntity {
    @Id
    private String id = UUID.randomUUID().toString();
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime = new Date();
    @NotEmpty(message = ApplicationMessage.ROLE_NAME_ARGS_NOT_FOUND)
    private String name;
    private String description;
}
