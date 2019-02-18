package com.rengu.cosimulation.enums;

/**
 * Author: XYmar
 * Date: 2019/2/13 13:58
 * 异常状态码
 */
public enum ResultCode {
    SUCCESS(0, "请求成功"),
    WEAK_NET_WORK(-1, "网络异常，请稍后重试"),
    // 角色相关    1000x
    ROLE_ARGS_NOT_FOUND_ERROR(10001, "未发现角色参数"),
    ROLE_NAME_ARGS_NOT_FOUND_ERROR(10002, "角色名称参数不存在或不合法"),
    ROLE_NAME_NOT_FOUND_ERROR(10003, "未发现该角色名称"),
    ROLE_NAME_EXISTED_ERROR(10004, "该角色名称已存在"),
    ROLE_ID_NOT_FOUND_ERROR(10005,"未发现该角色ID"),
    ROLE_CHANGE_NOT_SUPPORT_ERROR(10006, "该角色不可修改"),

    // 用户相关    1001x
    USER_ARGS_NOT_FOUND_ERROR(10011, "未发现用户参数"),
    USER_USERNAME_ARGS_NOT_FOUND_ERROR(10012, "用户名称参数不存在或不合法"),
    USER_USERNAME_NOT_FOUND_ERROR(10013, "未发现该用户名称"),
    USER_PASSWORD_ARGS_NOT_FOUND_ERROR(10014, "用户密码参数不存在或不合法"),
    USER_ID_NOT_FOUND_ERROR(10015,"未发现该用户ID"),
    USER_USERNAME_EXISTED_ERROR(10016,"该用户名已存在"),
    USER_ENABLED_NOT_SUPPORT_ERROR(10017,"不支持的权限类型"),
    PARAMETER_ERROR(10101, "参数错误");

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
