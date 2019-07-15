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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    public void doAfterReturning(JoinPoint joinPoint, Result result) throws JsonProcessingException {
        // 获取Http请求对象
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest httpServletRequest = servletRequestAttributes.getRequest();
        if (httpServletRequest.getUserPrincipal() != null) {
            String mainOperatorName = null;
            String arrangedPersonName = null;
            int messageOperate = ApplicationConfig.ARRANGE_NONE_OPERATE;
            int mainBody = ApplicationConfig.MAINBODY_NONE;
            String description = "";
            // 用户接口
            if (joinPoint.getTarget().getClass().equals(UserController.class)) {
                String type = result.getData().getClass().toString();
                if(type.equals("class java.util.ArrayList") || type.equals("class java.lang.Boolean")){
                    return;
                }
                Users users = (Users) result.getData();
                mainOperatorName = userService.getUserByUsername("admin").getUsername();                         // 操作人
                arrangedPersonName = users.getUsername();                                                   // 被操作人
                switch (joinPoint.getSignature().getName()) {
                    case "distributeUserById": {
                        messageOperate = ApplicationConfig.ARRANGE_ROLE_OPERATE;
                        mainBody = ApplicationConfig.MAINBODY_Users;                        // 对用户的操作
                        StringBuilder rolename= new StringBuilder();
                        for(Role role : users.getRoleEntities()){
                            rolename.append(role.getDescription()).append(" ");
                        }
                        description = "系统管理员已将您的角色更新为：" + rolename;
                        break;
                    }
                    case "updateSecretClassById": {
                        messageOperate = ApplicationConfig.MODIFY_OPERATE;
                        mainBody = ApplicationConfig.MAINBODY_Users;                        // 对用户的操作
                        description = "安全保密员已将您的密级更新为：" + users.getSecretClass();
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
                Project project = (Project) result.getData();
                mainOperatorName = project.getCreator().getUsername();                         // 操作人
                arrangedPersonName = project.getPic().getUsername();                           // 被操作人
                switch (joinPoint.getSignature().getName()) {
                    case "saveProject": {
                        messageOperate = ApplicationConfig.ARRANGE_PROJECTPIC_OPERATE;
                        mainBody = ApplicationConfig.MAINBODY_Project;         // 对项目的操作
                        description = mainOperatorName + "创建了项目" + project.getName() + "， 并指定您为项目负责人";
                        break;
                    }
                    case "arrangeProject": {
                        messageOperate = ApplicationConfig.MODIFY_OPERATE;
                        mainBody = ApplicationConfig.MAINBODY_Project;         // 对项目的操作
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String finishTime = sdf.format(new Date(Long.valueOf(project.getFinishTime())));
                        description = project.getName() + " 项目令号更新为：" + project.getOrderNum() + "，项目节点更新为：" + finishTime;
                        break;
                    }
                    case "deleteProjectById": {
                        messageOperate = ApplicationConfig.DELETE_OPERATE;
                        mainBody = ApplicationConfig.MAINBODY_Project;         // 对项目的操作
                        description = "项目" + project.getName() + " 已移入回收站";
                        break;
                    }
                    case "restoreProjectById": {
                        messageOperate = ApplicationConfig.RESTORE_OPERATE;
                        mainBody = ApplicationConfig.MAINBODY_Project;         // 对项目的操作
                        description = "项目" +  project.getName() + " 已恢复";
                        break;
                    }
                    case "startProject": {
                        messageOperate = ApplicationConfig.MODIFY_OPERATE;
                        mainBody = ApplicationConfig.MAINBODY_Project;         // 对项目的操作
                        description = "项目" +  project.getName() + "已启动";
                        break;
                    }
                    case "updateProjectPic": {             // 修改项目负责人
                        messageOperate = ApplicationConfig.ARRANGE_PROJECTPIC_OPERATE;
                        mainBody = ApplicationConfig.MAINBODY_Project;         // 对项目的操作
                        description = mainOperatorName + "  指定您为项目负责人";
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
                Subtask subtask = (Subtask) result.getData();
                switch (joinPoint.getSignature().getName()) {
                    case "updateSubtaskById": {
                        mainOperatorName = subtask.getProject().getPic().getUsername();                 // 操作人
                        arrangedPersonName = subtask.getUsers().getUsername();                           // 被操作人
                        mainBody = ApplicationConfig.MAINBODY_Subtask;           // 对子任务的操作
                        messageOperate = ApplicationConfig.ARRANGE_SUBTASKPIC_OPERATE;
                        description = mainOperatorName + "将您指定为子任务  " + subtask.getName() + "  的负责人";
                        break;
                    }
                    case "arrangeAssessorsByIds": {
                        messageOperate = ApplicationConfig.ARRANGE_AUDIT_OPERATE;
                        mainBody = ApplicationConfig.MAINBODY_Subtask;           // 对子任务的操作
                        mainOperatorName = subtask.getUsers().getUsername();
                        Set<Users> proofreadUsersSet = subtask.getProofSet();
                        Set<Users> auditUsersSet = subtask.getAuditSet();
                        Set<Users> countersignUsersSet = subtask.getCountSet();
                        Set<Users> approveUsersSet = subtask.getApproveSet();
                        proofreadUsersSet.addAll(auditUsersSet);
                        proofreadUsersSet.addAll(countersignUsersSet);
                        proofreadUsersSet.addAll(approveUsersSet);

                        List<Message> messageList = new ArrayList<>();
                        for(Users users : proofreadUsersSet){
                            Message message = new Message();
                            message.setMainOperatorName(mainOperatorName);
                            message.setArrangedPersonName(users.getUsername());
                            message.setMessageOperate(messageOperate);
                            message.setMainBody(mainBody);
                            message.setDescription(subtask.getUsers().getUsername() + "指定您为子任务 " + subtask.getProject().getName() + "的审核人员");
                            messageList.add(message);
                        }
                        messageRepository.saveAll(messageList);
                        break;
                    }
                    case "handleModifyApply": {
                        mainOperatorName = subtask.getProject().getPic().getUsername();                 // 操作人
                        arrangedPersonName = subtask.getUsers().getUsername();                           // 被操作人
                        mainBody = ApplicationConfig.MAINBODY_Subtask;           // 对子任务的操作
                        messageOperate = ApplicationConfig.MODIFY_OPERATE;
                        description = "您的二次修改申请  " + (subtask.isIfModifyApprove() ? "已通过" : "未通过");
                        break;
                    }
                    case "subtaskAudit": {
                        if(subtask.isIfApprove()){
                            mainOperatorName = null;                 // 操作人
                            arrangedPersonName = subtask.getUsers().getUsername();                           // 被操作人
                            mainBody = ApplicationConfig.MAINBODY_Subtask;           // 对子任务的操作
                            messageOperate = ApplicationConfig.MODIFY_OPERATE;
                            description = "您在项目  " + subtask.getProject().getName() + " 中的子任务  "  + subtask.getName() + "已审核通过，相关文件已入库";
                        }
                        if(subtask.isIfReject()){
                            mainOperatorName = null;                 // 操作人
                            arrangedPersonName = subtask.getUsers().getUsername();                           // 被操作人
                            mainBody = ApplicationConfig.MAINBODY_Subtask;           // 对子任务的操作
                            messageOperate = ApplicationConfig.MODIFY_OPERATE;
                            description = "您在项目  " + subtask.getProject().getName() + " 中的子任务  "  + subtask.getName() + "已被驳回";
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
                SubDepotFile subDepotFile = (SubDepotFile) result.getData();
                switch (joinPoint.getSignature().getName()) {
                    case "arrangeAudit": {
                        messageOperate = ApplicationConfig.ARRANGE_AUDIT_OPERATE;
                        mainBody = ApplicationConfig.MAINBODY_SUBLIBRARY_FILE_ENTITY;
                        Set<Users> subLibraryProofreadUsersSet = subDepotFile.getProofSet();
                        Set<Users> auditUsersSet = subDepotFile.getAuditSet();
                        Set<Users> countersignUsersSet = subDepotFile.getCountSet();
                        Set<Users> approveUsersSet = subDepotFile.getApproveSet();
                        subLibraryProofreadUsersSet.addAll(auditUsersSet);
                        subLibraryProofreadUsersSet.addAll(countersignUsersSet);
                        subLibraryProofreadUsersSet.addAll(approveUsersSet);

                        List<Message> messageList = new ArrayList<>();
                        for(Users users : subLibraryProofreadUsersSet){
                            Message message = new Message();
                            message.setMainOperatorName(subDepotFile.getUsers().getUsername());
                            message.setArrangedPersonName(users.getUsername());
                            message.setMessageOperate(messageOperate);
                            message.setMainBody(mainBody);
                            message.setDescription(subDepotFile.getUsers().getUsername() + "指定您为文件 " + subDepotFile.getName() + "的审核人员");
                            messageList.add(message);
                        }
                        messageRepository.saveAll(messageList);

                        break;
                    }
                    case "handleModifyApply": {
                        mainOperatorName = userService.getUserByUsername("admin").getUsername();                 // 操作人
                        arrangedPersonName = subDepotFile.getUsers().getUsername();                           // 被操作人
                        mainBody = ApplicationConfig.MAINBODY_SUBLIBRARY_FILE_ENTITY;           // 对子库文件的操作
                        messageOperate = ApplicationConfig.MODIFY_OPERATE;
                        description = "您的二次修改申请  " + (subDepotFile.isIfModifyApprove() ? "已通过" : "未通过");
                        break;
                    }
                    case "sublibraryFileAudit": {
                        if(subDepotFile.isIfApprove()){
                            mainOperatorName = null;                 // 操作人
                            arrangedPersonName = subDepotFile.getUsers().getUsername();                           // 被操作人
                            messageOperate = ApplicationConfig.MODIFY_OPERATE;
                            mainBody = ApplicationConfig.MAINBODY_SUBLIBRARY_FILE_ENTITY;           // 对子库文件的操作
                            description = "您上传的文件  " + subDepotFile.getName() + "已审核通过，相关文件已入库";
                        }
                        if(subDepotFile.isIfReject()){
                            mainOperatorName = null;                 // 操作人
                            arrangedPersonName = subDepotFile.getUsers().getUsername();                           // 被操作人
                            messageOperate = ApplicationConfig.MODIFY_OPERATE;
                            mainBody = ApplicationConfig.MAINBODY_SUBLIBRARY_FILE_ENTITY;           // 对子库文件的操作
                            description = "您上传的文件  " + subDepotFile.getName() + "已被驳回";
                        }
                        break;
                    }
                    default:
                }
            }
            if (messageOperate != ApplicationConfig.ARRANGE_NONE_OPERATE || !StringUtils.isEmpty(description)) {
                Message message = new Message();
                message.setMainOperatorName(mainOperatorName);
                message.setArrangedPersonName(arrangedPersonName);
                message.setMessageOperate(messageOperate);
                message.setMainBody(mainBody);
                message.setDescription(description);
                messageService.saveMessage(message);
            }
            // List<Message> messageEntityList = messageService.getMessagesByUser(userService.getUserByUsername(username).getId());
            // Long count = messageRepository.countByArrangedPersonAndIfRead(userService.getUserByUsername(username), false);
            List<Users> usersList = userService.getAll();
            for(Users users : usersList){          // 向所有用户推送消息
                Long count = messageRepository.countByArrangedPersonNameAndIfRead(users.getUsername(), false);
                simpMessagingTemplate.convertAndSend("/personalInfo/" + users.getUsername(), ResultUtils.success(count));
            }
        }
    }

}
