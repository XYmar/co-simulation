package com.rengu.cosimulation.service;

import com.rengu.cosimulation.entity.MessageEntity;
import com.rengu.cosimulation.entity.UserEntity;
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
    public MessageEntity saveMessage(MessageEntity messageEntity){
        return messageRepository.save(messageEntity);
    }

    // 标记已读
    public MessageEntity readAlready(String messageId){
        MessageEntity messageEntity = getMessageById(messageId);
        messageEntity.setIfRead(true);
        return messageRepository.save(messageEntity);
    }

    // 全部已读
    public List<MessageEntity> readAll(String userId){
        List<MessageEntity> messageEntityList = getMessagesByUser(userId);
        for(MessageEntity messageEntity : messageEntityList){
            messageEntity.setIfRead(true);
        }
        return messageRepository.saveAll(messageEntityList);
    }

    // 清空所有已读通知
    public List<MessageEntity> clearAllRead(String userId){
        UserEntity userEntity = userService.getUserById(userId);
        List<MessageEntity> messageEntityList = messageRepository.findByArrangedPersonAndIfRead(userEntity, true);
        messageRepository.deleteAll(messageEntityList);
        return messageEntityList;
    }

    // 根据状态查询通知: 查看所有已读未读消息
    public List<MessageEntity> findByIfRead(String userId, boolean ifRead){
        UserEntity userEntity = userService.getUserById(userId);
        return messageRepository.findByArrangedPersonAndIfRead(userEntity, ifRead);
    }

    // 查看全部通知，根据被操作用户返回消息
    public List<MessageEntity> getMessagesByUser(String userId){
        UserEntity userEntity = userService.getUserById(userId);
        return messageRepository.findByArrangedPerson(userEntity);
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
    public MessageEntity getMessageById(String messageId) {
        if(!hasMessageById(messageId)){
            throw new ResultException(ResultCode.MESSAGE_ID_NOT_FOUND_ERROR);
        }
        return messageRepository.findById(messageId).get();
    }

}
