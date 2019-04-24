package com.rengu.cosimulation.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rengu.cosimulation.controller.ProjectController;
import com.rengu.cosimulation.controller.SublibraryFilesController;
import com.rengu.cosimulation.controller.SubtaskController;
import com.rengu.cosimulation.controller.UserController;
import com.rengu.cosimulation.entity.*;
import com.rengu.cosimulation.repository.MessageRepository;
import com.rengu.cosimulation.service.MessageService;
import com.rengu.cosimulation.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Author: XYmar
 * Date: 2019/4/22 10:02
 */

@Aspect
@Component
@Slf4j
public class MessageAop {
    private final MessageRepository messageRepository;
    private final MessageService messageService;
    private final UserService userService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    public MessageAop(MessageService messageService, SimpMessagingTemplate simpMessagingTemplate, UserService userService, MessageRepository messageRepository) {
        this.messageService = messageService;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.userService = userService;
        this.messageRepository = messageRepository;
    }

    @Pointcut(value = "execution(public * com.rengu.cosimulation.controller..*(..))")
    private void requestPonitCut() {
    }

    @AfterReturning(pointcut = "requestPonitCut()", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, ResultEntity result) throws JsonProcessingException {
        // 获取Http请求对象
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest httpServletRequest = servletRequestAttributes.getRequest();
        if (httpServletRequest.getUserPrincipal() != null) {
            String username = httpServletRequest.getUserPrincipal().getName();
            UserEntity mainOperator = new UserEntity();
            UserEntity arrangedPerson = new UserEntity();
            int messageOperate = ApplicationConfig.ARRANGE_NONE_OPERATE;
            int mainBody = ApplicationConfig.MAINBODY_NONE;
            String description = "";
            // 用户接口
            if (joinPoint.getTarget().getClass().equals(UserController.class)) {
                String type = result.getData().getClass().toString();
                if(type.equals("class java.util.ArrayList") || type.equals("class java.lang.Boolean")){
                    return;
                }
                UserEntity userEntity = (UserEntity) result.getData();
                mainOperator = userService.getUserByUsername("admin");                         // 操作人
                arrangedPerson = userEntity;                                                   // 被操作人
                switch (joinPoint.getSignature().getName()) {
                    case "distributeUserById": {
                        mainBody = ApplicationConfig.ARRANGE_ROLE_OPERATE;
                        messageOperate = ApplicationConfig.MAINBODY_USERENTITY;                        // 对用户的操作
                        description = "系统管理员已将您的角色更新为：" + userEntity.getRoleEntities().toString();
                        break;
                    }
                    case "updateSecretClassById": {
                        mainBody = ApplicationConfig.MODIFY_OPERATE;
                        messageOperate = ApplicationConfig.MAINBODY_USERENTITY;                        // 对用户的操作
                        description = "安全保密员已将您的密级更新为：" + userEntity.getSecretClass();
                        break;
                    }
                    default:
                }
            }
            // 工程接口
            if (joinPoint.getTarget().getClass().equals(ProjectController.class)) {
                String type = result.getData().getClass().toString();
                if(type.equals("class java.util.ArrayList") || type.equals("class java.lang.Boolean")){
                   return;
                }
                ProjectEntity projectEntity = (ProjectEntity) result.getData();
                mainOperator = projectEntity.getCreator();                         // 操作人
                arrangedPerson = projectEntity.getPic();                           // 被操作人
                switch (joinPoint.getSignature().getName()) {
                    case "saveProject": {
                        mainBody = ApplicationConfig.ARRANGE_PROJECTPIC_OPERATE;
                        messageOperate = ApplicationConfig.MAINBODY_PROJECTENTITY;         // 对项目的操作
                        description = mainOperator.getUsername() + "创建了" + projectEntity.getName() + "， 并指定您为项目负责人";
                        break;
                    }
                    case "arrangeProject": {
                        mainBody = ApplicationConfig.MODIFY_OPERATE;
                        messageOperate = ApplicationConfig.MAINBODY_PROJECTENTITY;         // 对项目的操作
                        description = projectEntity.getName() + " 项目令号更新为：" + projectEntity.getOrderNum() + "，项目节点更新为：" + projectEntity.getFinishTime();
                        break;
                    }
                    case "deleteProjectById": {
                        mainBody = ApplicationConfig.DELETE_OPERATE;
                        messageOperate = ApplicationConfig.MAINBODY_PROJECTENTITY;         // 对项目的操作
                        description = projectEntity.getName() + " 已移入回收站";
                        break;
                    }
                    case "restoreProjectById": {
                        mainBody = ApplicationConfig.RESTORE_OPERATE;
                        messageOperate = ApplicationConfig.MAINBODY_PROJECTENTITY;         // 对项目的操作
                        description = projectEntity.getName() + " 已恢复";
                        break;
                    }
                    case "startProject": {
                        mainBody = ApplicationConfig.MODIFY_OPERATE;
                        messageOperate = ApplicationConfig.MAINBODY_PROJECTENTITY;         // 对项目的操作
                        description = projectEntity.getName() + "已启动";
                        break;
                    }
                    case "updateProjectPic": {             // 修改项目负责人
                        mainBody = ApplicationConfig.ARRANGE_PROJECTPIC_OPERATE;
                        messageOperate = ApplicationConfig.MAINBODY_PROJECTENTITY;         // 对项目的操作
                        description = mainOperator.getUsername() + "  指定您为项目负责人";
                        break;
                    }
                    default:
                }
            }
            // 子任务接口
            if (joinPoint.getTarget().getClass().equals(SubtaskController.class)) {
                String type = result.getData().getClass().toString();
                if(type.equals("class java.util.ArrayList") || type.equals("class java.lang.Boolean") || type.equals("class java.util.HashMap")){
                    return;
                }
                SubtaskEntity subtaskEntity = (SubtaskEntity) result.getData();
                switch (joinPoint.getSignature().getName()) {
                    case "updateSubtaskById": {
                        mainOperator = subtaskEntity.getProjectEntity().getPic();                 // 操作人
                        arrangedPerson = subtaskEntity.getUserEntity();                           // 被操作人
                        messageOperate = ApplicationConfig.MAINBODY_SUBTASKENTITY;           // 对子任务的操作
                        mainBody = ApplicationConfig.ARRANGE_PROJECTPIC_OPERATE;
                        description = mainOperator.getUsername() + "将您指定为子任务  " + subtaskEntity.getName() + "  的负责人";
                        break;
                    }
                    case "arrangeAssessorsByIds": {
                        mainBody = ApplicationConfig.MODIFY_OPERATE;
                        mainOperator = subtaskEntity.getUserEntity();
                        Set<UserEntity> proofreadUserSet = subtaskEntity.getProofreadUserSet();
                        Set<UserEntity> auditUserSet = subtaskEntity.getAuditUserSet();
                        Set<UserEntity> countersignUserSet = subtaskEntity.getCountersignUserSet();
                        Set<UserEntity> approveUserSet = subtaskEntity.getApproveUserSet();
                        proofreadUserSet.addAll(auditUserSet);
                        proofreadUserSet.addAll(countersignUserSet);
                        proofreadUserSet.addAll(approveUserSet);

                        List<MessageEntity> messageEntityList = new ArrayList<>();
                        for(UserEntity userEntity : proofreadUserSet){
                            MessageEntity messageEntity = new MessageEntity();
                            messageEntity.setMainOperator(mainOperator);
                            messageEntity.setArrangedPerson(userEntity);
                            messageEntity.setMessageOperate(messageOperate);
                            messageEntity.setMainBody(mainBody);
                            messageEntity.setDescription(subtaskEntity.getUserEntity().getUsername() + "指定您为 " + subtaskEntity.getProjectEntity().getName() + "的审核人员");
                            messageEntityList.add(messageEntity);
                        }
                        messageRepository.saveAll(messageEntityList);
                        break;
                    }
                    case "handleModifyApply": {
                        mainOperator = subtaskEntity.getProjectEntity().getPic();                 // 操作人
                        arrangedPerson = subtaskEntity.getUserEntity();                           // 被操作人
                        messageOperate = ApplicationConfig.MAINBODY_SUBTASKENTITY;           // 对子任务的操作
                        mainBody = ApplicationConfig.MODIFY_OPERATE;
                        description = "您的二次修改申请  " + (subtaskEntity.isIfModifyApprove() ? "已通过" : "未通过");
                        break;
                    }
                    case "subtaskAudit": {
                        mainOperator = null;                 // 操作人
                        arrangedPerson = subtaskEntity.getUserEntity();                           // 被操作人
                        messageOperate = ApplicationConfig.MAINBODY_SUBTASKENTITY;           // 对子任务的操作
                        mainBody = ApplicationConfig.MODIFY_OPERATE;
                        if(subtaskEntity.isIfApprove()){
                            description = "您的在项目  " + subtaskEntity.getProjectEntity().getName() + " 中的子任务  "  + subtaskEntity.getName() + "已审核通过，相关文件已入库";
                        }
                        if(subtaskEntity.isIfReject()){
                            description = "您的在项目  " + subtaskEntity.getProjectEntity().getName() + " 中的子任务  "  + subtaskEntity.getName() + "已被驳回";
                        }
                        break;
                    }
                    default:
                }
            }
            // 子库文件接口
            if (joinPoint.getTarget().getClass().equals(SublibraryFilesController.class)) {
                String type = result.getData().getClass().toString();
                if(type.equals("class java.util.ArrayList") || type.equals("class java.lang.Boolean") || type.equals("class java.util.HashMap")){
                    return;
                }
                SublibraryFilesEntity sublibraryFilesEntity = (SublibraryFilesEntity) result.getData();
                switch (joinPoint.getSignature().getName()) {
                    case "arrangeAudit": {
                        mainBody = ApplicationConfig.MODIFY_OPERATE;
                        Set<UserEntity> subLibraryProofreadUserSet = sublibraryFilesEntity.getProofreadUserSet();
                        Set<UserEntity> auditUserSet = sublibraryFilesEntity.getAuditUserSet();
                        Set<UserEntity> countersignUserSet = sublibraryFilesEntity.getCountersignUserSet();
                        Set<UserEntity> approveUserSet = sublibraryFilesEntity.getApproveUserSet();
                        subLibraryProofreadUserSet.addAll(auditUserSet);
                        subLibraryProofreadUserSet.addAll(countersignUserSet);
                        subLibraryProofreadUserSet.addAll(approveUserSet);

                        List<MessageEntity> messageEntityList = new ArrayList<>();
                        for(UserEntity userEntity : subLibraryProofreadUserSet){
                            MessageEntity messageEntity = new MessageEntity();
                            messageEntity.setMainOperator(sublibraryFilesEntity.getUserEntity());
                            messageEntity.setArrangedPerson(userEntity);
                            messageEntity.setMessageOperate(messageOperate);
                            messageEntity.setMainBody(mainBody);
                            messageEntity.setDescription(sublibraryFilesEntity.getUserEntity().getUsername() + "指定您为文件 " + sublibraryFilesEntity.getName() + "的审核人员");
                            messageEntityList.add(messageEntity);
                        }
                        messageRepository.saveAll(messageEntityList);

                        break;
                    }
                    case "handleModifyApply": {
                        mainOperator = userService.getUserByUsername("admin");                 // 操作人
                        arrangedPerson = sublibraryFilesEntity.getUserEntity();                           // 被操作人
                        messageOperate = ApplicationConfig.MAINBODY_SUBLIBRARY_FILE_ENTITY;           // 对子库文件的操作
                        mainBody = ApplicationConfig.MODIFY_OPERATE;
                        description = "您的二次修改申请  " + (sublibraryFilesEntity.isIfModifyApprove() ? "已通过" : "未通过");
                        break;
                    }
                    case "sublibraryFileAudit": {
                        mainOperator = null;                 // 操作人
                        arrangedPerson = sublibraryFilesEntity.getUserEntity();                           // 被操作人
                        mainBody = ApplicationConfig.MODIFY_OPERATE;
                        messageOperate = ApplicationConfig.MAINBODY_SUBLIBRARY_FILE_ENTITY;           // 对子库文件的操作
                        if(sublibraryFilesEntity.isIfApprove()){
                            description = "您上传的文件  " + sublibraryFilesEntity.getName() + "已审核通过，相关文件已入库";
                        }
                        if(sublibraryFilesEntity.isIfReject()){
                            description = "您上传的文件  " + sublibraryFilesEntity.getName() + "已被驳回";
                        }
                        break;
                    }
                    default:
                }
            }
            if (messageOperate != ApplicationConfig.ARRANGE_NONE_OPERATE || !StringUtils.isEmpty(description)) {
                MessageEntity messageEntity = new MessageEntity();
                messageEntity.setMainOperator(mainOperator);
                messageEntity.setArrangedPerson(arrangedPerson);
                messageEntity.setMessageOperate(messageOperate);
                messageEntity.setMainBody(mainBody);
                messageService.saveMessage(messageEntity);
            }
            List<MessageEntity> messageEntityList = messageService.getMessagesByUser(userService.getUserByUsername(username).getId());
            simpMessagingTemplate.convertAndSend("/personalInfo/" + username, ResultUtils.success(messageEntityList));
        }
    }

}
