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
    public static final String DEFAULT_USER_ROLE_NAME = "user";        //安全审计员
    // 客户目前新加三个可变角色
    public static final String DEFAULT_PROJECT_MANAGER_ROLE_NAME = "project_manager";          //项目管理员
    public static final String DEFAULT_FILE_AUDITOR__ROLE_NAME = "file_auditor";               //仿真技术文件审核员
    public static final String DEFAULT_NORMAL_DESIGNER_ROLE_NAME = "normal_designer";          //一般仿真设计员

    // 默认的系统管理员
    public static final String DEFAULT_ADMIN_USERNAME = "admin";
    public static final String DEFAULT_ADMIN_PASSWORD = "admin";

    // 默认设计环节
    public static final String DEFAULT_STRUCTURAL_MODELING_NAME = "structural_modeling";             //结构建模
    public static final String DEFAULT_ELECTRICAL_MODELING_NAME = "electrical_modeling";             //电气建模
    public static final String DEFAULT_STRUCTURAL_SIMULATION_NAME = "structural_simulation";         //结构仿真
    public static final String DEFAULT_ELECTRICAL_SIMULATION_NAME = "electrical_simulation";         //电气仿真
    public static final String DEFAULT_THERMAL_SIMULATION_NAME = "thermal_simulation";               //热学仿真
    public static final String DEFAULT_MECHANICAL_SIMULATION_NAME = "mechanical_simulation";         //力学仿真
    public static final String DEFAULT_ASSEMBLY_SIMULATION_NAME = "assembly_simulation";             //结构建模

    // 文件块保存路径
    public static final String CHUNKS_SAVE_PATH = FormatUtils.formatPath(FileUtils.getTempDirectoryPath() + File.separator + "SIMULATION" + File.separator + "CHUNKS");
    // 文件保存路径
    public static final String FILES_SAVE_PATH = FormatUtils.formatPath(FileUtils.getUserDirectoryPath() + File.separator + "SIMULATION" + File.separator + "FILES");


}
