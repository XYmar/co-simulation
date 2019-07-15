package com.rengu.cosimulation.service;

import com.rengu.cosimulation.entity.Message;
import com.rengu.cosimulation.entity.Users;
import com.rengu.cosimulation.enums.ResultCode;
import com.rengu.cosimulation.exception.ResultException;
import com.rengu.cosimulation.repository.MessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Author: XYmar
 * Date: 2019/4/22 16:01
 */
@Service
@Slf4j
public class MessageService {
    private final MessageRepository messageRepository;
    private final UserService userService;

    @Autowired
    public MessageService(MessageRepository messageRepository, UserService userService) {
        this.messageRepository = messageRepository;
        this.userService = userService;
    }

    // 保存通知
    public Message saveMessage(Message message){
        return messageRepository.save(message);
    }

    // 标记已读
    public Message readAlready(String messageId){
        Message message = getMessageById(messageId);
        message.setIfRead(true);
        return messageRepository.save(message);
    }

    // 全部已读
    public List<Message> readAll(String userId){
        List<Message> messageList = getMessagesByUser(userId);
        for(Message message : messageList){
            message.setIfRead(true);
        }
        return messageRepository.saveAll(messageList);
    }

    // 清空所有已读通知
    public List<Message> clearAllRead(String userId){
        Users users = userService.getUserById(userId);
        List<Message> messageList = messageRepository.findByArrangedPersonNameAndIfRead(users.getUsername(), true);
        messageRepository.deleteAll(messageList);
        return messageList;
    }

    // 根据状态查询通知: 查看所有已读未读消息
    public List<Message> findByIfRead(String userId, boolean ifRead){
        Users users = userService.getUserById(userId);
        return messageRepository.findByArrangedPersonNameAndIfRead(users.getUsername(), ifRead);
    }

    // 查看全部通知，根据被操作用户返回消息
    public List<Message> getMessagesByUser(String userId){
        Users users = userService.getUserById(userId);
        return messageRepository.findByArrangedPersonName(users.getUsername());
    }

    // 根据Id查询用户是否存在
    public boolean hasMessageById(String messageId) {
        if (StringUtils.isEmpty(messageId)) {
            return false;
        }
        return messageRepository.existsById(messageId);
    }

    // 根据id查询用户
    @Cacheable(value = "User_Cache", key = "#messageId")
    public Message getMessageById(String messageId) {
        if(!hasMessageById(messageId)){
            throw new ResultException(ResultCode.MESSAGE_ID_NOT_FOUND_ERROR);
        }
        return messageRepository.findById(messageId).get();
    }

}
