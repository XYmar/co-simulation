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
    USER_SECRETCLASS_NOT_FOUND_ERROR(10018,"请指定用户密级"),
    USER_ROLE_NOT_FOUND_ERROR(10019,"请指定用户角色"),
    USER_SECRETCLASS_NOT_SUPPORT_ERROR(10020,"用户权限不够，请重新指定"),

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
    PROJECT_SECRETCLASS_NOT_FOUND_ERROR(20010,"请指定项目密级"),
    PROJECT_ALREADY_START_ERROR(20011,"项目已启动，请勿重复操作"),

    // 设计环节相关    2100x
    DESIGN_LINK_ARGS_NOT_FOUND_ERROR(21001, "未发现设计环节参数"),
    DESIGN_LINK_NAME_ARGS_NOT_FOUND_ERROR(21002, "设计环节名称参数不存在或不合法"),
    DESIGN_LINK_NAME_NOT_FOUND_ERROR(21003, "未发现该设计环节名称"),
    DESIGN_LINK_NAME_EXISTED_ERROR(21004,"该设计环节名称已存在"),
    DESIGN_LINK_ID_NOT_FOUND_ERROR(21005,"未发现该设计环节ID"),

    // 子任务相关    2200x
    SUBTASK_ARGS_NOT_FOUND_ERROR(22001, "未发现子任务参数"),
    SUBTASK_NAME_ARGS_NOT_FOUND_ERROR(22002, "子任务名称参数不存在或不合法"),
    SUBTASK_NAME_NOT_FOUND_ERROR(22003, "未发现该子任务名称"),
    SUBTASK_NAME_EXISTED_ERROR(22004,"该子任务称已存在"),
    SUBTASK_ID_NOT_FOUND_ERROR(22005,"未发现该子任务ID"),
    SUBTASK_FINISH_TIME_NOT_FOUND_ERROR(22006,"未发现该子任务节点"),
    SUBTASK_ASSESSORS_NOT_FOUND_ERROR(22007,"请指定子任务审核人"),
    SUBTASK_USER_ARRANGE_AUTHORITY_DENIED_ERROR(22008,"无权指定审核人"),
    SUBTASK_STATE_NOT_FOUND_ERROR(22009,"请传入审核结果"),
    SUBTASK_PARENT_NOT_ALL_OVER(22010,"请等待上一流程结束再提交"),
    SUBTASK_HAVE_NOT_START(22011,"此任务当前阶段无法执行上传操作"),
    SUBTASK_USER_HAVE_NO_AUTHORITY_TO_ARRANGE(22012,"无权指定子任务负责人"),


    // 子任务文件相关  2300
    SUBTASK_FILE_ID_NOT_FOUND_ERROR(23001,"未发现该子任务文件ID"),
    SUBTASK_FILE_DOWNLOAD_DENIED_ERROR(23002,"无权下载此文件"),
    SUBTASK_FILE_ARGS_NOT_FOUND_ERROR(23003,"未发现子任务文件参数"),
    SUBTASK_FILE_SECRETCLASS_NOT_SUPPORT_ERROR(23004,"子任务文件只能低于或等于项目密级"),

    // 文件块相关    3000x
    FILE_CHUNK_NOT_FOUND_ERROR(30001, "文件块不存在或不合法"),
    FILE_CHUNK_EXISTED_ERROR(30002, "该文件块已存在"),
    FILE_MD5_NOT_FOUND_ERROR(30003, "未发现该文件MD5"),
    FILE_MD5_EXISTED_ERROR(30004,"该文件MD5已存在"),
    FILE_ID_NOT_FOUND_ERROR(30005,"未发现该文件Id"),

    // 流程节点相关   2400x
    PROCESS_NODE_ID_NOT_FOUND_ERROR(24001,"未发现该节点ID"),
    PROCESS_NODE_NOT_FOUND_ERROR(24002, "项目流程不存在"),
    PROCESS_ARGS_NOT_FOUND_ERROR(24003, "流程节点参数不存在"),

    // 库相关   2500x
    LIBRARY_ID_NOT_FOUND_ERROR(25001, "未发现该库id"),

    // 子库相关   2600x
    SUBLIBRARY_ID_NOT_FOUND_ERROR(26001, "未发现该子库id"),
    SUBLIBRARY_TYPE_EXISTED_ERROR(26002, "该子库类型已存在"),
    SUBLIBRARY_NOT__APPOINT_ERROR(26003, "请指定库"),

    // 子库文件相关   2700x
    SUBLIBRARY_FILE_ID_NOT_FOUND_ERROR(27001, "未发现该子库文件id"),
    SUBLIBRARY_FILE_DOWNLOAD_DENIED_ERROR(27002,"无权下载此文件"),
    SUBLIBRARY_FILE_ARGS_NOT_FOUND_ERROR(27003,"未发现子库文件参数"),
    SUBLIBRARY_FILE_STATE_NOT_FOUND_ERROR(27004,"请指明当前审核阶段"),
    SUBLIBRARY_FILE_UPLOAD_DENIED(27004,"无权上传比自己密级高的文件"),

    // 驳回相关2800x
    FILE_MODIFYWAY_NOT_FOUND_ERROR(28001,"请传入修改方式"),
    FILE_VERSION_NOT_FOUND_ERROR(28002,"请选择文件更新的版本"),
    MODIFY_APPROVE_NOT_PASS_ERROR(28003,"您的二次修改申请还未通过，请耐心等待"),
    FILE_HAS_NO_REVOKE_FILE(28004,"当前文件无可撤销版本"),
    SUBLIBRARY_FILE_VERSION_NOT_CHOOSE_ERROR(28005,"请选择恢复至哪个版本"),

    // 审核相关   2900x
    AUDITMODE_NOT_FOUND_ERROR(29001,"请选择模式"),
    PROOFREADUSERS_NOT_FOUND_ERROR(20002,"请选择校对人"),
    AUDITUSERS_NOT_FOUND_ERROR(29003,"请选择审核人"),
    COUNTERSIGNUSERS_NOT_FOUND_ERROR(29004,"请选择会签人"),
    APPROVEUSERS_NOT_FOUND_ERROR(29005,"请选择批准人"),
    STATE_NOT_FOUND_ERROR(29006,"请指明当前审核阶段"),
    IFPASS_NOT_FOUND_ERROR(29007,"请传入当前阶段审核结果"),
    USER_PASS_DENIED_ERROR(29008,"自己无权限通过"),
    USER_ALREADY_COUNTERSIGN_ERROR(29009,"该文件您已处理过"),
    CURRENT_PROGRESS_NOT_ARRIVE_ERROR(29010,"还未进行到当前流程"),

    // 修改删除权限控制 3000x
    DELETE_DENIED_ERROR(30013,"当前阶段无法进行删除操作"),
    MODIFY_DENIED_ERROR(30014,"当前阶段无法进行修改操作"),
    SECOND_MODIFY_DENIED_ERROR(30015,"请等待任务审核结束再提交二次修改申请"),
    ARRANGE_DENIED_ERROR(30016,"请等待任务审核结束再提交二次修改申请"),

    // 设备相关3100x
    DEVICE_ID_NOT_FOUND(31001, "未找到该设备"),
    DEVICE_IS_OFFLINE(31002, "该设备已离线"),
    SCAN_DISK_TIME_OUT(31003, "获取设备磁盘信息超时"),
    SCAN_PROCESS_TIME_OUT(31004, "获取设备进程信息超时"),

    // 通知相关   5000x
    MESSAGE_ID_NOT_FOUND_ERROR(50015,"未发现该消息ID"),

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
