package com.rengu.cosimulation.service;

import com.rengu.cosimulation.entity.*;
import com.rengu.cosimulation.enums.ResultCode;
import com.rengu.cosimulation.exception.ResultException;
import com.rengu.cosimulation.repository.SublibraryFilesAuditRepository;
import com.rengu.cosimulation.repository.SublibraryFilesHistoryRepository;
import com.rengu.cosimulation.repository.SublibraryFilesRepository;
import com.rengu.cosimulation.repository.SubtaskFilesRepository;
import com.rengu.cosimulation.utils.ApplicationConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Author: XYmar
 * Date: 2019/4/1 10:50
 */
@Slf4j
@Service
public class SublibraryFilesService {
    private final SublibraryFilesRepository sublibraryFilesRepository;
    private final SublibraryService sublibraryService;
    private final FileService fileService;
    private final UserService userService;
    private final SublibraryFilesAuditRepository sublibraryFilesAuditRepository;
    private final SublibraryFilesHistoryRepository sublibraryFilesHistoryRepository;
    private final SubtaskFilesRepository subtaskFilesRepository;

    @Autowired
    public SublibraryFilesService(SublibraryFilesRepository sublibraryFilesRepository, FileService fileService, SublibraryService sublibraryService, UserService userService, SublibraryFilesAuditRepository sublibraryFilesAuditRepository, SublibraryFilesHistoryRepository sublibraryFilesHistoryRepository, SubtaskFilesRepository subtaskFilesRepository) {
        this.sublibraryFilesRepository = sublibraryFilesRepository;
        this.fileService = fileService;
        this.sublibraryService = sublibraryService;
        this.userService = userService;
        this.sublibraryFilesAuditRepository = sublibraryFilesAuditRepository;
        this.sublibraryFilesHistoryRepository = sublibraryFilesHistoryRepository;
        this.subtaskFilesRepository = subtaskFilesRepository;
    }

    // 根据名称、后缀及子库检查文件是否存在
    public boolean hasSublibraryFilesByNameAndExtensionAndSublibraryEntity(String name, String postfix, SublibraryEntity sublibraryEntity) {
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(postfix)) {
            return false;
        }
        return sublibraryFilesRepository.existsByNameAndPostfixAndSublibraryEntity(name, postfix, sublibraryEntity);
    }

    // 根据名称、后缀及子库查询文件
    public SublibraryFilesEntity getSublibraryFilesByNameAndPostfixAndSublibraryEntity(String name, String postfix, SublibraryEntity sublibraryEntity) {
        return sublibraryFilesRepository.findByNameAndPostfixAndSublibraryEntity(name, postfix, sublibraryEntity).get();
    }

    // 根据子库id创建文件 （后台不判断节点是否存在）
    @CacheEvict(value = "SublibraryFiles_Cache", allEntries = true)
    public List<SublibraryFilesEntity> saveSublibraryFilesBySublibraryId(String sublibraryId, String userId, List<FileMetaEntity> fileMetaEntityList) {
        if(!sublibraryService.hasSublibraryById(sublibraryId)){
            throw new ResultException(ResultCode.SUBLIBRARY_ID_NOT_FOUND_ERROR);
        }
        SublibraryEntity sublibraryEntity = sublibraryService.getSublibraryById(sublibraryId);
        List<SublibraryFilesEntity> sublibraryFilesEntityList = new ArrayList<>();
        for (FileMetaEntity fileMetaEntity : fileMetaEntityList) {
            SublibraryFilesEntity sublibraryFilesEntity = new SublibraryFilesEntity();
            sublibraryFilesEntity.setName(StringUtils.isEmpty(FilenameUtils.getBaseName(fileMetaEntity.getRelativePath())) ? "-" : FilenameUtils.getBaseName(fileMetaEntity.getRelativePath()));
            sublibraryFilesEntity.setPostfix(FilenameUtils.getExtension(fileMetaEntity.getRelativePath()));
            sublibraryFilesEntity.setType(fileMetaEntity.getType());
            sublibraryFilesEntity.setSecretClass(fileMetaEntity.getSecretClass());
            sublibraryFilesEntity.setProductNo(fileMetaEntity.getProductNo());
            sublibraryFilesEntity.setFileNo(fileMetaEntity.getFileNo());
            sublibraryFilesEntity.setVersion("M1");
            sublibraryFilesEntity.setIfApprove(false);
            sublibraryFilesEntity.setIfReject(false);
            sublibraryFilesEntity.setManyCounterSignState(0);           // 多人会签模式，此时无人开始会签
            sublibraryFilesEntity.setUserEntity(userService.getUserById(userId));
            sublibraryFilesEntity.setFileEntity(fileService.getFileById(fileMetaEntity.getFileId()));
            sublibraryFilesEntity.setSublibraryEntity(sublibraryEntity);
            sublibraryFilesEntityList.add(sublibraryFilesRepository.save(sublibraryFilesEntity));
        }
        return sublibraryFilesEntityList;
    }

    // 子任务入库
    public void stockIn(SubtaskEntity subtaskEntity){
        List<SubtaskFilesEntity> subtaskFilesEntityList = subtaskFilesRepository.findBySubTaskEntity(subtaskEntity);    // 子任务下的文件
        List<SublibraryFilesEntity> sublibraryFilesEntityList = new ArrayList<>();
        for(SubtaskFilesEntity subtaskFilesEntity : subtaskFilesEntityList){
            // 子任务文件属于几个子库
            Set<SublibraryEntity> sublibraryEntitySet = subtaskFilesEntity.getSublibraryEntitySet();
            for(SublibraryEntity sublibraryEntity : sublibraryEntitySet){
                SublibraryFilesEntity sublibraryFilesEntity = new SublibraryFilesEntity();
                sublibraryFilesEntity.setName(subtaskFilesEntity.getName());
                sublibraryFilesEntity.setPostfix(subtaskFilesEntity.getPostfix());
                sublibraryFilesEntity.setType(subtaskFilesEntity.getType());
                sublibraryFilesEntity.setSecretClass(subtaskFilesEntity.getSecretClass());
                sublibraryFilesEntity.setProductNo(subtaskFilesEntity.getProductNo());
                sublibraryFilesEntity.setFileNo(subtaskFilesEntity.getFileNo());
                sublibraryFilesEntity.setVersion(subtaskFilesEntity.getVersion());
                sublibraryFilesEntity.setIfApprove(true);
                sublibraryFilesEntity.setIfReject(false);
                sublibraryFilesEntity.setManyCounterSignState(0);
                sublibraryFilesEntity.setUserEntity(subtaskEntity.getUserEntity());
                sublibraryFilesEntity.setFileEntity(subtaskFilesEntity.getFileEntity());
                sublibraryFilesEntity.setSublibraryEntity(sublibraryEntity);
                sublibraryFilesEntityList.add(sublibraryFilesEntity);
            }
        }
        sublibraryFilesRepository.saveAll(sublibraryFilesEntityList);

    }
    /*@CacheEvict(value = "SublibraryFiles_Cache", allEntries = true)
    public List<SublibraryFilesEntity> saveSublibraryFilesBySublibraryId(String sublibraryId, String userId, List<FileMetaEntity> fileMetaEntityList) {
        if(!sublibraryService.hasSublibraryById(sublibraryId)){
            throw new ResultException(ResultCode.SUBLIBRARY_ID_NOT_FOUND_ERROR);
        }
        SublibraryEntity sublibraryEntity = sublibraryService.getSublibraryById(sublibraryId);
        List<SublibraryFilesEntity> sublibraryFilesEntityList = new ArrayList<>();
        for (FileMetaEntity fileMetaEntity : fileMetaEntityList) {
            String path = fileMetaEntity.getRelativePath().split("/")[1];

            // 判断该节点是否存在
            if (hasSublibraryFilesByNameAndExtensionAndSublibraryEntity(FilenameUtils.getBaseName(path), FilenameUtils.getExtension(path), sublibraryEntity)) {
                SublibraryFilesEntity sublibraryFilesEntity = getSublibraryFilesByNameAndPostfixAndSublibraryEntity(FilenameUtils.getBaseName(path), FilenameUtils.getExtension(path), sublibraryEntity);
                sublibraryFilesEntity.setCreateTime(new Date());
                sublibraryFilesEntity.setName(FilenameUtils.getBaseName(fileMetaEntity.getRelativePath()));
                sublibraryFilesEntity.setPostfix(FilenameUtils.getExtension(fileMetaEntity.getRelativePath()));
                sublibraryFilesEntity.setType(fileMetaEntity.getType());
                sublibraryFilesEntity.setSecretClass(fileMetaEntity.getSecretClass());
                sublibraryFilesEntity.setProductNo(fileMetaEntity.getProductNo());
                sublibraryFilesEntity.setFileNo(fileMetaEntity.getFileNo());
                sublibraryFilesEntity.setVersion("M1");
                sublibraryFilesEntity.setIfApprove(false);
                sublibraryFilesEntity.setIfReject(false);
                sublibraryFilesEntity.setManyCounterSignState(0);          // 多人会签模式，此时无人开始会签
                sublibraryFilesEntity.setUserEntity(userService.getUserById(userId));
                sublibraryFilesEntity.setFileEntity(fileService.getFileById(fileMetaEntity.getFileId()));
                sublibraryFilesEntityList.add(sublibraryFilesRepository.save(sublibraryFilesEntity));
            } else {
                SublibraryFilesEntity sublibraryFilesEntity = new SublibraryFilesEntity();
                sublibraryFilesEntity.setName(StringUtils.isEmpty(FilenameUtils.getBaseName(fileMetaEntity.getRelativePath())) ? "-" : FilenameUtils.getBaseName(fileMetaEntity.getRelativePath()));
                sublibraryFilesEntity.setPostfix(FilenameUtils.getExtension(fileMetaEntity.getRelativePath()));
                sublibraryFilesEntity.setType(fileMetaEntity.getType());
                sublibraryFilesEntity.setSecretClass(fileMetaEntity.getSecretClass());
                sublibraryFilesEntity.setProductNo(fileMetaEntity.getProductNo());
                sublibraryFilesEntity.setFileNo(fileMetaEntity.getFileNo());
                sublibraryFilesEntity.setVersion("M1");
                sublibraryFilesEntity.setIfApprove(false);
                sublibraryFilesEntity.setIfReject(false);
                sublibraryFilesEntity.setManyCounterSignState(0);           // 多人会签模式，此时无人开始会签
                sublibraryFilesEntity.setUserEntity(userService.getUserById(userId));
                sublibraryFilesEntity.setFileEntity(fileService.getFileById(fileMetaEntity.getFileId()));
                sublibraryFilesEntity.setSublibraryEntity(sublibraryEntity);
                sublibraryFilesEntityList.add(sublibraryFilesRepository.save(sublibraryFilesEntity));
            }
        }
        return sublibraryFilesEntityList;
    }
*/
    // 根据子库id查询子库下的文件
    public List<SublibraryFilesEntity> getSublibraryFilesBySublibraryAndIfApprove(String sublibraryId, boolean ifApprove) {
        if(!sublibraryService.hasSublibraryById(sublibraryId)){
            throw new ResultException(ResultCode.SUBLIBRARY_ID_NOT_FOUND_ERROR);
        }
        return sublibraryFilesRepository.findBySublibraryEntityAndIfApprove(sublibraryService.getSublibraryById(sublibraryId), ifApprove);
    }

    // 根据id查询子库文件是否存在
    public boolean hasSublibraryFileById(String sublibraryFileId) {
        if (StringUtils.isEmpty(sublibraryFileId)) {
            return false;
        }
        return sublibraryFilesRepository.existsById(sublibraryFileId);
    }

    // 根据文件id查询子库文件
    @Cacheable(value = "SublibraryFiles_Cache", key = "#sublibraryFileId")
    public SublibraryFilesEntity getSublibraryFileById(String sublibraryFileId) {
        if (!hasSublibraryFileById(sublibraryFileId)) {
            throw new ResultException(ResultCode.SUBLIBRARY_ID_NOT_FOUND_ERROR);
        }
        return sublibraryFilesRepository.findById(sublibraryFileId).get();
    }

    // 根据子库文件的id下载文件
    public File exportSublibraryFileById(String sublibraryFileId, String userId) throws IOException {
        if(!userService.hasUserById(userId)){
            throw new ResultException(ResultCode.USER_ID_NOT_FOUND_ERROR);
        }
        int userSecretClass = userService.getUserById(userId).getSecretClass();     //获取用户密级
        if(!hasSublibraryFileById(sublibraryFileId)){
            throw new ResultException(ResultCode.SUBLIBRARY_FILE_ID_NOT_FOUND_ERROR);
        }
        int fileSecretClass = getSublibraryFileById(sublibraryFileId).getSecretClass();
        // 用户只能下载小于等于自己密级的文件
        if(userSecretClass < fileSecretClass){
            throw new ResultException(ResultCode.SUBLIBRARY_FILE_DOWNLOAD_DENIED_ERROR);
        }
        SublibraryFilesEntity sublibraryFilesEntity = getSublibraryFileById(sublibraryFileId);
        File exportFile = new File(FileUtils.getTempDirectoryPath() + File.separator + sublibraryFilesEntity.getName() + "." + sublibraryFilesEntity.getFileEntity().getPostfix());
        FileUtils.copyFile(new File(sublibraryFilesEntity.getFileEntity().getLocalPath()), exportFile);
        return exportFile;
    }

    // 根据子任务文件id修改文件基本信息(类型、密级、代号)
    public SublibraryFilesEntity updateSublibraryFileId(String sublibraryFileId, SublibraryFilesEntity sublibraryFilesEntityArgs) {
        if(!hasSublibraryFileById(sublibraryFileId)){
            throw new ResultException(ResultCode.SUBLIBRARY_FILE_ID_NOT_FOUND_ERROR);
        }
        SublibraryFilesEntity sublibraryFilesEntity = getSublibraryFileById(sublibraryFileId);
        if(sublibraryFilesEntityArgs == null){
            throw new ResultException(ResultCode.SUBLIBRARY_FILE_ARGS_NOT_FOUND_ERROR);
        }
        if(!StringUtils.isEmpty(sublibraryFilesEntityArgs.getType())){
            sublibraryFilesEntity.setType(sublibraryFilesEntityArgs.getType());
        }
        if(!StringUtils.isEmpty(sublibraryFilesEntityArgs.getSecretClass())){
            sublibraryFilesEntity.setSecretClass(sublibraryFilesEntityArgs.getSecretClass());
        }
        if(!StringUtils.isEmpty(sublibraryFilesEntityArgs.getProductNo())){
            sublibraryFilesEntity.setProductNo(sublibraryFilesEntityArgs.getProductNo());
        }
        if(!StringUtils.isEmpty(sublibraryFilesEntityArgs.getFileNo())){
            sublibraryFilesEntity.setFileNo(sublibraryFilesEntityArgs.getFileNo());
        }
        return sublibraryFilesRepository.save(sublibraryFilesEntity);
    }

    // 根据子库文件id删除文件
    public SublibraryFilesEntity deleteSublibraryFileId(String sublibraryFileId) {
        if(!hasSublibraryFileById(sublibraryFileId)){
            throw new ResultException(ResultCode.SUBLIBRARY_FILE_ID_NOT_FOUND_ERROR);
        }
        SublibraryFilesEntity sublibraryFilesEntity = getSublibraryFileById(sublibraryFileId);
        sublibraryFilesRepository.delete(sublibraryFilesEntity);
        return sublibraryFilesEntity;
    }

    // 选择文件（一批文件）的审核模式及四类审核人
    public List<SublibraryFilesEntity> arrangeAudit(String[] sublibraryFileId, int auditMode, String[] proofreadUserIds, String[] auditUserIds, String[] countersignUserIds, String[] approveUserIds){
        if(StringUtils.isEmpty(sublibraryFileId)){
            throw new ResultException(ResultCode.SUBLIBRARY_FILE_ID_NOT_FOUND_ERROR);
        }
        if(StringUtils.isEmpty(String.valueOf(auditMode))){
            throw new ResultException(ResultCode.AUDITMODE_NOT_FOUND_ERROR);
        }
        if(ArrayUtils.isEmpty(proofreadUserIds)){
            throw new ResultException(ResultCode.PROOFREADUSERS_NOT_FOUND_ERROR);
        }
        if(ArrayUtils.isEmpty(auditUserIds)){
            throw new ResultException(ResultCode.AUDITUSERS_NOT_FOUND_ERROR);
        }
        if(auditMode != ApplicationConfig.AUDIT_NO_COUNTERSIGN){
            if(ArrayUtils.isEmpty(countersignUserIds)){
                throw new ResultException(ResultCode.COUNTERSIGNUSERS_NOT_FOUND_ERROR);
            }
        }
        if(ArrayUtils.isEmpty(approveUserIds)){
            throw new ResultException(ResultCode.APPROVEUSERS_NOT_FOUND_ERROR);
        }
        List<SublibraryFilesEntity> sublibraryFilesEntityList = new ArrayList<>();
        for(String id : sublibraryFileId){
            if(!hasSublibraryFileById(id)){
                throw new ResultException(ResultCode.SUBLIBRARY_FILE_ID_NOT_FOUND_ERROR);
            }
            SublibraryFilesEntity sublibraryFilesEntity = getSublibraryFileById(id);
            sublibraryFilesEntity.setProofreadUserSet(idsToSet(proofreadUserIds));
            sublibraryFilesEntity.setAuditUserSet(idsToSet(auditUserIds));
            if(auditMode != ApplicationConfig.AUDIT_NO_COUNTERSIGN){
                sublibraryFilesEntity.setCountersignUserSet(idsToSet(countersignUserIds));
            }
            sublibraryFilesEntity.setApproveUserSet(idsToSet(approveUserIds));
            sublibraryFilesEntity.setAuditMode(auditMode);
            sublibraryFilesEntityList.add(sublibraryFilesEntity);
        }

        return sublibraryFilesRepository.saveAll(sublibraryFilesEntityList);
    }

    // 根据用户id数组，将用户数组转为set集合
    private Set<UserEntity> idsToSet(String[] ids){
        Set<UserEntity> userEntities = new HashSet<>();
        if(!ArrayUtils.isEmpty(ids)){
            for(String id : ids){
                userEntities.add(userService.getUserById(id));
            }
        }
        return userEntities;
    }

    // 根据用户id查询待校对、待审核、待会签、待批准
    public Map<String, List> findToBeAuditedFilesByUserId(String userId){
        UserEntity userEntity = userService.getUserById(userId);
        Map<String, List> sublibraryFilesToBeAudited = new HashMap<>();
        sublibraryFilesToBeAudited.put("proofreadFiles", sublibraryFilesRepository.findByProofreadUserSetContaining(userEntity));
        sublibraryFilesToBeAudited.put("auditFiles", sublibraryFilesRepository.findByAuditUserSetContaining(userEntity));
        sublibraryFilesToBeAudited.put("countersignFiles", sublibraryFilesRepository.findByCountersignUserSetContaining(userEntity));
        sublibraryFilesToBeAudited.put("approveFiles", sublibraryFilesRepository.findByApproveUserSet(userEntity));
        sublibraryFilesToBeAudited.put("alreadyAudit", sublibraryFilesAuditRepository.findByUserEntityAndState(userEntity, ApplicationConfig.SUBLIBRARY_FILE_COUNTERSIGN));
        return sublibraryFilesToBeAudited;
    }

    // 审核操作
    public SublibraryFilesEntity sublibraryFileAudit(String sublibraryFileId, String userId, SublibraryFilesEntity sublibraryFilesEntityArgs, SublibraryFilesAuditEntity sublibraryFilesAuditEntityArgs){
        SublibraryFilesEntity sublibraryFilesEntity = getSublibraryFileById(sublibraryFileId);
        UserEntity userEntity = userService.getUserById(userId);             // 登录的用户

        if(StringUtils.isEmpty(String.valueOf(sublibraryFilesEntityArgs.getState()))){
            throw new ResultException(ResultCode.SUBLIBRARY_FILE_STATE_NOT_FOUND_ERROR);
        }

        if(sublibraryFilesEntityArgs.getState() == 1){        // 校对中设置状态为校对
            sublibraryFilesEntity.setState(ApplicationConfig.SUBLIBRARY_FILE_PROOFREAD);
        }
        if(sublibraryFilesEntityArgs.getState() != sublibraryFilesEntity.getState()){
            throw new ResultException(ResultCode.CURRENT_PROGRESS_NOT_ARRIVE_ERROR);
        }
        SublibraryFilesAuditEntity sublibraryFilesAuditEntity = new SublibraryFilesAuditEntity();      // 审核详情

        /**
         *  子库文件流程控制：
         *  当前审核结果： pass --> 当前审核人：自己--> 报错，无通过权限
         *                                      其他人--> 设置子库文件进入下一模式：
         *                                                当前阶段为审核时-->  1)无会签：审核-->批准
         *                                                                     2)一人/多人会签：审核-->会签
         *                                                当前阶段为会签时-->  1)一人会签：会签-->批准（同上）
         *          *                                                          2)多人会签：多人审核通过审核-->批准
         *                                                                                 若当前用户已会签过则报错，您已会签过
         *                                                当前阶段为批准时-->  修改子文件的通过状态为true； 设置子文件状态为审批结束
         *                 no   --> 停留当前模式   --> 设置子文件状态为审批结束
         *                                             记录当前驳回的阶段
         * */
        // 若当前用户已审批过过则报错，您已执行过审批操作
        if(sublibraryFilesAuditRepository.existsBySublibraryFilesEntityAndUserEntityAndStateAndIfOver(sublibraryFilesEntity,userEntity,sublibraryFilesEntityArgs.getState(), false)){
            throw new ResultException(ResultCode.USER_ALREADY_COUNTERSIGN_ERROR);
        }

        if(sublibraryFilesAuditEntityArgs.isIfPass()){
            if(sublibraryFilesEntity.getUserEntity().getId().equals(userId)){    // 自己无权通过
                throw new ResultException(ResultCode.USER_PASS_DENIED_ERROR);
            }
            if(sublibraryFilesEntityArgs.getState() == ApplicationConfig.SUBLIBRARY_FILE_AUDIT){               // 当前为审核
                if(sublibraryFilesEntityArgs.getAuditMode() == ApplicationConfig.SUBLIBRARY_FILE_AUDIT_NO_COUNTERSIGN){  // 无会签
                    sublibraryFilesEntity.setState(ApplicationConfig.SUBLIBRARY_FILE_APPROVE);
                }else{
                    sublibraryFilesEntity.setState(ApplicationConfig.SUBLIBRARY_FILE_COUNTERSIGN);
                }
                sublibraryFilesAuditEntity.setState(sublibraryFilesEntityArgs.getState());
            }else if(sublibraryFilesEntityArgs.getState() == ApplicationConfig.SUBLIBRARY_FILE_COUNTERSIGN && sublibraryFilesEntityArgs.getAuditMode() == ApplicationConfig.SUBLIBRARY_FILE_AUDIT_MANY_COUNTERSIGN){
                // 当前为会签 且模式为多人会签
                if((sublibraryFilesEntity.getManyCounterSignState() + 1) != sublibraryFilesEntity.getCountersignUserSet().size()){
                    sublibraryFilesEntity.setManyCounterSignState(sublibraryFilesEntity.getManyCounterSignState() + 1);
                }else{                          // 所有人都已会签过
                    sublibraryFilesEntity.setState(sublibraryFilesEntityArgs.getState() + 1);
                }
                sublibraryFilesAuditEntity.setState(sublibraryFilesEntityArgs.getState());
            }else if(sublibraryFilesEntityArgs.getState() == ApplicationConfig.SUBLIBRARY_FILE_APPROVE){           // 当前为批准
                sublibraryFilesEntity.setIfApprove(true);                                                          // 子库文件通过审核
                sublibraryFilesEntity.setState(ApplicationConfig.SUBLIBRARY_FILE_AUDIT_OVER);                          // 审批结束
                sublibraryFilesAuditEntity.setState(sublibraryFilesEntityArgs.getState());
            }else{
                sublibraryFilesEntity.setState(sublibraryFilesEntityArgs.getState() + 1);
                sublibraryFilesAuditEntity.setState(sublibraryFilesEntityArgs.getState());                         // 在哪步驳回
            }
        }else{                // 驳回
            sublibraryFilesEntity.setState(ApplicationConfig.SUBLIBRARY_FILE_AUDIT_OVER);                          // 审批结束
            sublibraryFilesAuditEntity.setState(sublibraryFilesEntityArgs.getState());
            sublibraryFilesEntity.setRejectState(sublibraryFilesEntityArgs.getState());

            sublibraryFilesEntity.setIfReject(true);           // 设置驳回状态为true
        }
        /**
         * 审核模式、审核阶段、审核结果、审核人、审核意见、当前步骤结束
         * */
        sublibraryFilesAuditEntity.setIfPass(sublibraryFilesAuditEntityArgs.isIfPass());               // 审核结果
        sublibraryFilesAuditEntity.setUserEntity(userEntity);                                          // 审核人
        sublibraryFilesAuditEntity.setSublibraryFilesEntity(sublibraryFilesEntity);                    // 审核详情所属子库文件
        sublibraryFilesAuditEntity.setAuditDescription(sublibraryFilesAuditEntityArgs.getAuditDescription());   // 审核意见
        sublibraryFilesAuditRepository.save(sublibraryFilesAuditEntity);

        // 批准通过后 或 驳回后, 将详情改为已结束
        if(!(sublibraryFilesAuditEntityArgs.isIfPass()) || sublibraryFilesEntityArgs.getState() == ApplicationConfig.SUBLIBRARY_FILE_APPROVE){
            List<SublibraryFilesAuditEntity> sublibraryFilesAuditEntityList = sublibraryFilesAuditRepository.findBySublibraryFilesEntity(sublibraryFilesEntity);
            for(SublibraryFilesAuditEntity sublibraryFilesAuditEntity1 : sublibraryFilesAuditEntityList){
                sublibraryFilesAuditEntity1.setIfOver(true);
            }
            sublibraryFilesAuditRepository.saveAll(sublibraryFilesAuditEntityList);
        }
        return sublibraryFilesEntity;
    }

    // 申请二次修改
    public SublibraryFilesEntity applyForModify(String sublibraryFileId){
        SublibraryFilesEntity sublibraryFilesEntity = getSublibraryFileById(sublibraryFileId);
        sublibraryFilesEntity.setState(ApplicationConfig.SUBLIBRARY_FILE_APPLY_FOR_MODIFY);
        return sublibraryFilesRepository.save(sublibraryFilesEntity);
    }

    // 系统管理员查询所有待审核的二次修改申请
    public List<SublibraryFilesEntity> findByState(){
        return sublibraryFilesRepository.findByState(ApplicationConfig.SUBLIBRARY_FILE_APPLY_FOR_MODIFY);
    }

    // 系统管理员处理二次修改申请
    public SublibraryFilesEntity handleModifyApply(String sublibraryFileId, boolean ifModifyApprove){
        SublibraryFilesEntity sublibraryFilesEntity = getSublibraryFileById(sublibraryFileId);
        sublibraryFilesEntity.setIfModifyApprove(ifModifyApprove);
        if(ifModifyApprove){
            sublibraryFilesEntity.setState(ApplicationConfig.SUBLIBRARY_FILE_APPLY_FOR_MODIFY_APPROVE);
        }else{
            sublibraryFilesEntity.setState(ApplicationConfig.SUBLIBRARY_FILE_AUDIT_OVER);
        }
        return sublibraryFilesRepository.save(sublibraryFilesEntity);
    }

    // 驳回后  修改  [id 是否是直接修改 驳回修改内容是否提交到第一个流程（直接修改需要） 文件 版本（二次修改需要）]
    public SublibraryFilesEntity modifySublibraryFile(String sublibraryFileId, FileMetaEntity fileMetaEntity){
        SublibraryFilesEntity sublibraryFilesEntity = getSublibraryFileById(sublibraryFileId);

        if(StringUtils.isEmpty(fileMetaEntity.isIfDirectModify())){
            throw new ResultException(ResultCode.SUBLIBRARY_FILE_MODIFYWAY_NOT_FOUND_ERROR);
        }
        if(fileMetaEntity.isIfDirectModify()){            // 直接修改
            // 修改前存储此文件的备份 若备份已存在删除上一备份
            if(sublibraryFilesHistoryRepository.existsByLeastSublibraryFilesEntityAndIfDirectModify(sublibraryFilesEntity, true)){
                sublibraryFilesHistoryRepository.delete(sublibraryFilesHistoryRepository.findByLeastSublibraryFilesEntityAndIfDirectModify(sublibraryFilesEntity, true).get(0));
            }
            saveSublibraryFilesHistoryBySublibraryFile(sublibraryFilesEntity, true);
            if(fileMetaEntity.isIfBackToStart()){                // 驳回后的修改提交到第一个流程
                sublibraryFilesEntity.setState(0);
            }else{
                sublibraryFilesEntity.setState(sublibraryFilesEntity.getRejectState());
            }

        }else{                 // 二次修改
            // 判断文件是否通过二次修改申请
            if(!sublibraryFilesEntity.isIfModifyApprove()){
                throw new ResultException(ResultCode.MODIFY_APPROVE_NOT_PASS_ERROR);
            }
            // 修改前保存此文件历史
            saveSublibraryFilesHistoryBySublibraryFile(sublibraryFilesEntity, false);

            // 二次修改
            if(StringUtils.isEmpty(fileMetaEntity.getVersion())){
                throw new ResultException(ResultCode.SUBLIBRARY_FILE_VERSION_NOT_FOUND_ERROR);
            }
            sublibraryFilesEntity.setState(0);
            sublibraryFilesEntity.setAuditMode(0);
            sublibraryFilesEntity.setIfModifyApprove(false);
            sublibraryFilesEntity.setIfApprove(false);

            // 四类审核人重置
            Set<UserEntity> userEntitySet = new HashSet<>();
            sublibraryFilesEntity.setVersion(fileMetaEntity.getVersion());
            sublibraryFilesEntity.setProofreadUserSet(userEntitySet);
            sublibraryFilesEntity.setAuditUserSet(userEntitySet);
            sublibraryFilesEntity.setCountersignUserSet(userEntitySet);
            sublibraryFilesEntity.setApproveUserSet(userEntitySet);
        }

        sublibraryFilesEntity.setName(StringUtils.isEmpty(FilenameUtils.getBaseName(fileMetaEntity.getRelativePath())) ? "-" : FilenameUtils.getBaseName(fileMetaEntity.getRelativePath()));
        sublibraryFilesEntity.setCreateTime(new Date());
        sublibraryFilesEntity.setPostfix(FilenameUtils.getExtension(fileMetaEntity.getRelativePath()));
        sublibraryFilesEntity.setType(fileMetaEntity.getType());
        sublibraryFilesEntity.setSecretClass(fileMetaEntity.getSecretClass());
        sublibraryFilesEntity.setProductNo(fileMetaEntity.getProductNo());
        sublibraryFilesEntity.setFileNo(fileMetaEntity.getFileNo());
        sublibraryFilesEntity.setIfApprove(false);
        sublibraryFilesEntity.setIfReject(false);
        sublibraryFilesEntity.setRejectState(0);
        sublibraryFilesEntity.setManyCounterSignState(0);           // 多人会签模式，此时无人开始会签
        sublibraryFilesEntity.setFileEntity(fileService.getFileById(fileMetaEntity.getFileId()));

        return sublibraryFilesEntity;
    }

    // 从子库文件生成子库文件历史
    public void saveSublibraryFilesHistoryBySublibraryFile(SublibraryFilesEntity sourceNode, boolean ifDirectModify) {
        SublibraryFilesHistoryEntity copyNode = new SublibraryFilesHistoryEntity();
        BeanUtils.copyProperties(sourceNode, copyNode, "id", "create_time", "leastSublibraryFilesEntity");
        copyNode.setLeastSublibraryFilesEntity(sourceNode);
        copyNode.setIfDirectModify(ifDirectModify);
        sublibraryFilesHistoryRepository.save(copyNode);
    }

    // 从子库文件历史生成子库文件
    public void saveSublibraryFilesBySublibraryFile(SublibraryFilesEntity coverNode, SublibraryFilesHistoryEntity sourceNode) {
        BeanUtils.copyProperties(sourceNode, coverNode, "id", "create_time", "leastSublibraryFilesEntity", "ifDirectModify", "proofreadUserSet", "auditUserSet", "countersignUserSet", "approveUserSet");
        /*Set<UserEntity> approveUserSet = sourceNode.getApproveUserSet();
        coverNode.setApproveUserSet(null);
        coverNode.setApproveUserSet(approveUserSet);*/
        sublibraryFilesRepository.save(coverNode);
    }

    // 根据子库文件id查询其临时文件是否存在
    public boolean ifHasTemp(String sublibraryFileId){
        return sublibraryFilesHistoryRepository.existsByLeastSublibraryFilesEntity(getSublibraryFileById(sublibraryFileId));
    }

    // 根据子库文件id查找其历史版本文件
    public List<SublibraryFilesHistoryEntity> getSublibraryHistoriesFiles(String sublibraryFileId){
        return sublibraryFilesHistoryRepository.findByLeastSublibraryFilesEntityAndIfDirectModify(getSublibraryFileById(sublibraryFileId), false);
    }

    // 撤销文件操作（直接修改）  更换版本（二次修改可恢复其中任意版本）   版本为空则为直接修改，否则为二次修改，切换到指定版本
    public SublibraryFilesEntity revokeModify(String sublibraryFileId, String version){
        if(!ifHasTemp(sublibraryFileId)){       // 当前文件不存在可撤销的文件
            throw new ResultException(ResultCode.SUBLIBRARY_FILE_HAS_NO_REVOKE_FILE);
        }
        // 当前文件
        SublibraryFilesEntity sublibraryFilesEntity = getSublibraryFileById(sublibraryFileId);
        // 需撤销至的文件
        SublibraryFilesHistoryEntity sourceNode;
        if(StringUtils.isEmpty(version)){         // 直接修改撤销，获取此目标文件的临时历史文件
            sourceNode = sublibraryFilesHistoryRepository.findByLeastSublibraryFilesEntityAndIfDirectModify(sublibraryFilesEntity, true).get(0);
        }else{    // 二次修改撤销，选择撤销到的版本
            sourceNode = sublibraryFilesHistoryRepository.findByLeastSublibraryFilesEntityAndIfDirectModifyAndVersion(sublibraryFilesEntity, false, version);
        }

        // 历史版本提为当前文件
        saveSublibraryFilesBySublibraryFile(sublibraryFilesEntity, sourceNode);
        sublibraryFilesHistoryRepository.delete(sourceNode);
        // 当前文件存为历史
        saveSublibraryFilesHistoryBySublibraryFile(sublibraryFilesEntity, true);

        return getSublibraryFileById(sublibraryFileId);
    }
}
