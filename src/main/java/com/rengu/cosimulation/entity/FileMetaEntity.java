package com.rengu.cosimulation.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Author: XYmar
 * Date: 2019/3/4 11:43
 */

@Data
public class FileMetaEntity implements Serializable {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime = new Date();
    private String fileId;
    private String name;
    private String relativePath;
    private String type;                  // 类型：  0：参数文件  1：模型文件  2：报告文件  3：实验数据
    private int secretClass;              // 密级：  0：公开  1：内部  2：秘密  3：机密  4：绝密
    private int codeName;                 // 代号：  0：产品型号  1：文件图号
}