package com.rengu.cosimulation.utils;

import org.apache.commons.io.FileUtils;

import java.io.File;

/**
 * Author: XYmar
 * Date: 2019/2/12 17:25
 */
public class ApplicationConfig {
    // 服务器连接地址、端口
    public static final int TCP_RECEIVE_PORT = 6005;
    public static final int UDP_RECEIVE_PORT = 6004;
    public static final int UDP_SEND_PORT = 3087;
    // 扫描超时时间
    public static final long SCAN_TIME_OUT = 1000 * 5;

    // 默认角色
    public static final String DEFAULT_ADMIN_ROLE_NAME = "admin";                              //系统管理员
    public static final String DEFAULT_SECURITY_GUARD_ROLE_NAME = "security_guard";            //安全保密员
    public static final String DEFAULT_SECURITY_AUDITOR_ROLE_NAME = "security_auditor";        //安全审计员
    public static final String DEFAULT_USER_ROLE_NAME = "user";        //普通用户
    // 客户目前新加三个可变角色
    public static final String DEFAULT_PROJECT_MANAGER_ROLE_NAME = "project_manager";          //项目管理员
    public static final String DEFAULT_FILE_AUDITOR__ROLE_NAME = "file_auditor";               //仿真技术文件审核员
    public static final String DEFAULT_NORMAL_DESIGNER_ROLE_NAME = "normal_designer";          //一般仿真设计员

    // 默认的系统管理员
    public static final String DEFAULT_ADMIN_USERNAME = "admin";
    public static final String DEFAULT_ADMIN_PASSWORD = "admin";
    // 默认的安全保密员
    public static final String DEFAULT_SECURITY_GUARD_USERNAME = "securityGuard";
    public static final String DEFAULT_SECURITY_GUARD_PASSWORD = "securityGuard";
    // 默认的安全审计员
    public static final String DEFAULT_SECURITY_AUDITOR_USERNAME = "securityAuditor";
    public static final String DEFAULT_SECURITY_AUDITOR_PASSWORD = "securityAuditor";

    // 默认设计环节
    public static final String DEFAULT_STRUCTURAL_MODELING_NAME = "structural_modeling";             //结构建模
    public static final String DEFAULT_ELECTRICAL_MODELING_NAME = "electrical_modeling";             //电气建模
    public static final String DEFAULT_STRUCTURAL_SIMULATION_NAME = "structural_simulation";         //结构仿真
    public static final String DEFAULT_ELECTRICAL_SIMULATION_NAME = "electrical_simulation";         //电气仿真
    public static final String DEFAULT_THERMAL_SIMULATION_NAME = "thermal_simulation";               //热学仿真
    public static final String DEFAULT_MECHANICAL_SIMULATION_NAME = "mechanical_simulation";         //力学仿真
    public static final String DEFAULT_ASSEMBLY_SIMULATION_NAME = "assembly_simulation";             //结构建模

    // 子任务状态
    public static final int SUBTASK_NOT_START = 0;                     // 子任务未开始
    public static final int SUBTASK_START = 1;                         // 子任务进行中
    public static final int SUBTASK_TO_BE_AUDIT = 2;                   // 待审批
    public static final int SUBTASK_PROOFREAD = 3;                     // 校对中
    public static final int SUBTASK_AUDIT = 4;                         // 审核中
    public static final int SUBTASK_COUNTERSIGN = 5;                   // 会签中
    public static final int SUBTASK_APPROVE = 6;                       // 批准中
    public static final int SUBTASK_AUDIT_OVER = 7;                    // 审批结束
    public static final int SUBTASK_APPLY_FOR_MODIFY = 8;              // 申请二次修改中
    public static final int SUBTASK_APPLY_FOR_MODIFY_APPROVE = 9;      // 二次修改中

    // 子任务提交审核方式： 第一次提交  直接修改  二次修改
    public static final int SUBTASK_FIRST_COMMIT = 0;                     // 第一次提交
    public static final int SUBTASK_DIRECT_MODIFY = 1;                    // 直接修改
    public static final int SUBTASK_SECOND_MODIFY = 2;                    // 二次修改

    // 子库文件审核环节
    public static final int SUBLIBRARY_FILE_PROOFREAD = 1;             // 校对中
    public static final int SUBLIBRARY_FILE_AUDIT = 2;                 // 审核中
    public static final int SUBLIBRARY_FILE_COUNTERSIGN = 3;           // 会签中
    public static final int SUBLIBRARY_FILE_APPROVE = 4;               // 批准中
    public static final int SUBLIBRARY_FILE_AUDIT_OVER = 5;                  // 审批结束
    public static final int SUBLIBRARY_FILE_APPLY_FOR_MODIFY = 6;      // 申请二次修改中
    public static final int SUBLIBRARY_FILE_APPLY_FOR_MODIFY_APPROVE = 7;      // 二次修改中

    public static final int SUBLIBRARY_FILE_IFAPPROVE = 1;                  // 审核通过
    public static final int SUBLIBRARY_FILE_IFREJECT = 2;               // 审核未通过

    // 子库文件 或子任务 审核模式 1：无会签  2：一人会签  3：多人会签
    public static final int SUBLIBRARY_FILE_AUDIT_NO_COUNTERSIGN = 1;               // 无会签
    public static final int SUBLIBRARY_FILE_AUDIT_ONE_COUNTERSIGN = 2;               // 一人会签
    public static final int SUBLIBRARY_FILE_AUDIT_MANY_COUNTERSIGN = 3;               // 多人会签

    // 子库文件 或子任务 审核模式 1：无会签  2：一人会签  3：多人会签
    public static final int AUDIT_NO_COUNTERSIGN = 1;               // 无会签
    public static final int AUDIT_ONE_COUNTERSIGN = 2;               // 一人会签
    public static final int AUDIT_MANY_COUNTERSIGN = 3;               // 多人会签

    // 子库文件上传  0：第一次上传  1： 直接修改  2： 二次修改
    public static final int SUBLIBRARY_FILE_FIRST_UPLOAD = 0;               // 子库文件第一次上传
    public static final int SUBLIBRARY_FILE_DIRECTOR_MODIFY = 1;               // 直接修改
    public static final int SUBLIBRARY_FILE_SECOND_MODIFY = 2;               // 二次修改

    // 通知消息
    // 通知的基本操作
    public static final int ARRANGE_NONE_OPERATE = 0;                     // 无操作
    public static final int ARRANGE_ROLE_OPERATE = 1;                     // 赋予新角色
    public static final int ARRANGE_PROJECTPIC_OPERATE = 2;               // 指定为项目负责人
    public static final int ARRANGE_SUBTASKPIC_OPERATE =3;               // 指定为子任务负责人
    public static final int ARRANGE_AUDIT_OPERATE = 4;                    // 指定为审核员
    public static final int DELETE_OPERATE = 5;                           // 删除
    public static final int RESTORE_OPERATE = 6;                          // 恢复
    public static final int MODIFY_OPERATE = 7;                           // 修改

    // 通知操作的主体
    public static final int MAINBODY_NONE = 0;                           // 无操作主体
    public static final int MAINBODY_USERENTITY = 1;                      // 用户
    public static final int MAINBODY_PROJECTENTITY = 2;                   // 项目
    public static final int MAINBODY_SUBTASKENTITY = 3;                   // 子任务
    public static final int MAINBODY_SUBLIBRARY_FILE_ENTITY = 4;                // 子库文件


    // 文件块保存路径
    public static final String CHUNKS_SAVE_PATH = FormatUtils.formatPath(FileUtils.getTempDirectoryPath() + File.separator + "SIMULATION" + File.separator + "CHUNKS");
    // 文件保存路径
    public static final String FILES_SAVE_PATH = FormatUtils.formatPath(FileUtils.getUserDirectoryPath() + File.separator + "SIMULATION" + File.separator + "FILES");


}
