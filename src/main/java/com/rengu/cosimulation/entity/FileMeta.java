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
public class FileMeta implements Serializable {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime = new Date();
    private String fileId;
    private String name;
    private String relativePath;
    private String type;                     // 类型：  0：参数文件  1：模型文件  2：报告文件  3：实验数据
    private int secretClass;                 // 密级：  0：公开  1：内部  2：秘密  3：机密  4：绝密
    private String productNo;                // 产品型号
    private String fileNo;                   // 文件图号
    private String sublibraryId;             // 子库id
    private String version;                  // 文件版本
    boolean ifDirectModify;                  // 是否直接修改
    boolean ifBackToStart;                   // 直接修改的话，是否回到第一个审批流程
}