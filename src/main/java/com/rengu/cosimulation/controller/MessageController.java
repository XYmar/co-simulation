package com.rengu.cosimulation.controller;

import com.rengu.cosimulation.entity.Result;
import com.rengu.cosimulation.service.MessageService;
import com.rengu.cosimulation.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Author: XYmar
 * Date: 2019/4/23 14:08
 */
@RestController
@RequestMapping(value = "/messages")
public class MessageController {
    private final MessageService messageService;

    @Autowired
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    // 查看全部通知
    @GetMapping("/{userId}")
    public Result getAll(@PathVariable(value = "userId") String userId){
        return ResultUtils.success(messageService.getMessagesByUser(userId));
    }

    // 标记某条通知已读
    @PatchMapping
    public Result readAlready(String messageId){
        return ResultUtils.success(messageService.readAlready(messageId));
    }

    // 标记某用户的所有通知全部已读
    @PatchMapping("/readAll")
    public Result readAll(String userId){
        return ResultUtils.success(messageService.readAll(userId));
    }

    // 清空所有已读通知
    @PostMapping(value = "/clearAllRead")
    public Result clearAllRead(String userId){
        return ResultUtils.success(messageService.clearAllRead(userId));
    }

    // 根据状态查询通知
    @PostMapping(value = "/findByIfRead")
    public Result findByIfRead(String userId, boolean ifRead){
        return ResultUtils.success(messageService.findByIfRead(userId, ifRead));
    }
}
