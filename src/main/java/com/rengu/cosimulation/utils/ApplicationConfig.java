package com.rengu.cosimulation.utils;

import org.apache.commons.io.FileUtils;

import java.io.File;

/**
 * Author: XYmar
 * Date: 2019/2/12 17:25
 */
public class ApplicationConfig {
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

    // 子任务文件审核环节
    public static final int PROOFREAD_ING = 2;  //  校对中
    public static final int AUDITOR_ING = 3;   //  审核中
    public static final int COUNTERSIGN_ING = 4; //  会签中
    public static final int APPROVER_ING = 5;  //  批准中
    public static final int ASSESS_FINISH = 6;  //  审核完成
    public static final int ASSESSOR_NOT_PASS = 0;  //  未通过
    public static final int ASSESSOR_PASS = 1;  // 已通过
    public static final int TO_AUDIT = 2;         //  待审核
    public static final int NOT_PEOPLE_COUNTERSIGNSTATE = 1;  //  无会签
    public static final int ONE_PEOPLE_COUNTERSIGNSTATE = 2;  //  一人会签
    public static final int MANY_PEOPLE_COUNTERSIGNSTATE = 3; //  多人会签
    public static final boolean ASSESS_STATE_NOT_PASS = false;  //  当前审核未通过
    public static final boolean ASSESS_STATE_PASS = true;  // 审核通过

    // 文件块保存路径
    public static final String CHUNKS_SAVE_PATH = FormatUtils.formatPath(FileUtils.getTempDirectoryPath() + File.separator + "SIMULATION" + File.separator + "CHUNKS");
    // 文件保存路径
    public static final String FILES_SAVE_PATH = FormatUtils.formatPath(FileUtils.getUserDirectoryPath() + File.separator + "SIMULATION" + File.separator + "FILES");


}
