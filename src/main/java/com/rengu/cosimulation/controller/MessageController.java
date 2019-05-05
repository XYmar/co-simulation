package com.rengu.cosimulation.controller;

import com.rengu.cosimulation.entity.ResultEntity;
import com.rengu.cosimulation.repository.MessageRepository;
import com.rengu.cosimulation.service.MessageService;
import com.rengu.cosimulation.service.UserService;
import com.rengu.cosimulation.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    public ResultEntity getAll(@PathVariable(value = "userId") String userId){
        return ResultUtils.success(messageService.getMessagesByUser(userId));
    }

    // 标记某条通知已读
    @PatchMapping
    public ResultEntity readAlready(String messageId){
        return ResultUtils.success(messageService.readAlready(messageId));
    }

    // 标记某用户的所有通知全部已读
    @PatchMapping("/readAll")
    public ResultEntity readAll(String userId){
        return ResultUtils.success(messageService.readAll(userId));
    }

    // 清空所有已读通知
    @PostMapping(value = "/clearAllRead")
    public ResultEntity clearAllRead(String userId){
        return ResultUtils.success(messageService.clearAllRead(userId));
    }

    // 根据状态查询通知
    @PostMapping(value = "/findByIfRead")
    public ResultEntity findByIfRead(String userId, boolean ifRead){
        return ResultUtils.success(messageService.findByIfRead(userId, ifRead));
    }
}
