package com.rengu.cosimulation.enums;

/**
 * Author: XYmar
 * Date: 2019/2/13 13:58
 * 异常状态码
 */
public enum ResultCode {
    SUCCESS(0, "请求成功"),
    WEAK_NET_WORK(-1, "请求有误，请重试"),
    // 角色相关    1000x
    ROLE_ARGS_NOT_FOUND_ERROR(10001, "未发现角色参数"),
    ROLE_NAME_ARGS_NOT_FOUND_ERROR(10002, "角色名称参数不存在或不合法"),
    ROLE_NAME_NOT_FOUND_ERROR(10003, "未发现该角色名称"),
    ROLE_NAME_EXISTED_ERROR(10004, "该角色名称已存在"),
    ROLE_ID_NOT_FOUND_ERROR(10005,"未发现该角色ID"),
    ROLE_CHANGE_NOT_SUPPORT_ERROR(10006, "不可对该角色进行操作"),

    // 用户相关    1001x
    USER_ARGS_NOT_FOUND_ERROR(10011, "未发现用户参数"),
    USER_USERNAME_ARGS_NOT_FOUND_ERROR(10012, "用户名称参数不存在或不合法"),
    USER_USERNAME_NOT_FOUND_ERROR(10013, "未发现该用户名称"),
    USER_PASSWORD_ARGS_NOT_FOUND_ERROR(10014, "用户密码参数不存在或不合法"),
    USER_ID_NOT_FOUND_ERROR(10015,"未发现该用户ID"),
    USER_USERNAME_EXISTED_ERROR(10016,"该用户名已存在"),
    USER_ENABLED_NOT_SUPPORT_ERROR(10017,"不支持的权限类型"),
    USER_ROLE_NOT_FOUND_ERROR(10018,"请指定用户角色"),

    // 项目相关    2000x
    PROJECT_ARGS_NOT_FOUND_ERROR(20001, "未发现项目参数"),
    PROJECT_NAME_ARGS_NOT_FOUND_ERROR(20002, "项目名称参数不存在或不合法"),
    PROJECT_CREATOR_ARGS_NOT_FOUND_ERROR(20003, "未发现创建者信息"),
    PROJECT_PIC_ARGS_NOT_FOUND_ERROR(20004, "请指定项目负责人"),
    PROJECT_ID_NOT_FOUND_ERROR(20005,"未发现该项目ID"),
    PROJECT_NAME_EXISTED_ERROR(20006,"该项目名已存在"),
    PROJECT_ENABLED_NOT_SUPPORT_ERROR(20007,"不支持的权限类型"),
    PROJECT_ORDER_NUM_NOT_FOUND_ERROR(20008,"未发现该项目令号"),
    PROJECT_FINISH_TIME_NOT_FOUND_ERROR(20009,"未发现该项目节点"),

    // 设计环节相关    2100x
    DESIGN_LINK_ARGS_NOT_FOUND_ERROR(21001, "未发现设计环节参数"),
    DESIGN_LINK_NAME_ARGS_NOT_FOUND_ERROR(21002, "设计环节名称参数不存在或不合法"),
    DESIGN_LINK_NAME_NOT_FOUND_ERROR(21003, "未发现该设计环节名称"),
    DESIGN_LINK_NAME_EXISTED_ERROR(21004,"该设计环节名称已存在"),
    DESIGN_LINK_ID_NOT_FOUND_ERROR(21005,"未发现该设计环节ID"),

    // 子任务相关    2200x
    PRODESIGN_LINK_ARGS_NOT_FOUND_ERROR(22001, "未发现子任务参数"),
    PRODESIGN_LINK_NAME_ARGS_NOT_FOUND_ERROR(22002, "子任务名称参数不存在或不合法"),
    PRODESIGN_LINK_NAME_NOT_FOUND_ERROR(22003, "未发现该子任务名称"),
    PRODESIGN_LINK_NAME_EXISTED_ERROR(22004,"该子任务称已存在"),
    PRODESIGN_LINK_ID_NOT_FOUND_ERROR(22005,"未发现该子任务ID"),
    PRODESIGN_LINK_FINISH_TIME_NOT_FOUND_ERROR(22006,"未发现该子任务节点"),
    PRODESIGN_LINK_ASSESSORS_NOT_FOUND_ERROR(22007,"请指定子任务审核人"),
    PRODESIGN_LINK_USER_ARRANGE_AUTHORITY_DENIED_ERROR(22008,"无权指定审核人"),

    // 子任务文件相关  2300
    PRODESIGN_LINK_FILE_ID_NOT_FOUND_ERROR(23001,"未发现该子任务文件ID"),
    PRODESIGN_LINK_FILE_DOWNLOAD_DENIED_ERROR(23002,"无权下载此文件"),
    PRODESIGN_LINK_FILE_ARGS_NOT_FOUND_ERROR(23003,"未发现子任务文件参数"),


    // 文件块相关    3000x
    FILE_CHUNK_NOT_FOUND_ERROR(30001, "文件块不存在或不合法"),
    FILE_CHUNK_EXISTED_ERROR(30002, "该文件块已存在"),
    FILE_MD5_NOT_FOUND_ERROR(30003, "未发现该文件MD5"),
    FILE_MD5_EXISTED_ERROR(30004,"该子任务称已存在"),
    FILE_ID_NOT_FOUND_ERROR(30005,"未发现该文件Id"),

    // 流程节点相关   2400x
    PROCESS_ARGS_NOT_FOUND_ERROR(24001, "流程节点参数不存在"),

    PARAMETER_ERROR(40001, "参数错误"),
    ACCESS_DENIED_ERROR(40002, "参数错误"),
    ARGS_NOT_FOUND_ERROR(40003, "未传递参数"),
    AUTHORITY_DENIED_ERROR(40004, "无权限进行该操作");

    private int code;
    private String msg;

    ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
