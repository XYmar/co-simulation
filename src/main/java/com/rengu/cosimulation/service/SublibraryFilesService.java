package com.rengu.cosimulation.service;

import com.rengu.cosimulation.entity.*;
import com.rengu.cosimulation.enums.ResultCode;
import com.rengu.cosimulation.exception.ResultException;
import com.rengu.cosimulation.repository.SublibraryFilesAuditRepository;
import com.rengu.cosimulation.repository.SublibraryFilesRepository;
import com.rengu.cosimulation.utils.ApplicationConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Autowired
    public SublibraryFilesService(SublibraryFilesRepository sublibraryFilesRepository, FileService fileService, SublibraryService sublibraryService, UserService userService, SublibraryFilesAuditRepository sublibraryFilesAuditRepository) {
        this.sublibraryFilesRepository = sublibraryFilesRepository;
        this.fileService = fileService;
        this.sublibraryService = sublibraryService;
        this.userService = userService;
        this.sublibraryFilesAuditRepository = sublibraryFilesAuditRepository;
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

    // 根据子库id创建文件
    @CacheEvict(value = "SublibraryFiles_Cache", allEntries = true)
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
            throw new ResultException(ResultCode.SUBLIBRARY_FILE_AUDITMODE_NOT_FOUND_ERROR);
        }
        if(ArrayUtils.isEmpty(proofreadUserIds)){
            throw new ResultException(ResultCode.SUBLIBRARY_FILE_PROOFREADUSERS_NOT_FOUND_ERROR);
        }
        if(ArrayUtils.isEmpty(auditUserIds)){
            throw new ResultException(ResultCode.SUBLIBRARY_FILE_AUDITUSERS_NOT_FOUND_ERROR);
        }
        if(auditMode != ApplicationConfig.SUBLIBRARY_FILE_AUDIT_NO_COUNTERSIGN){
            if(ArrayUtils.isEmpty(countersignUserIds)){
                throw new ResultException(ResultCode.SUBLIBRARY_FILE_COUNTERSIGNUSERS_NOT_FOUND_ERROR);
            }
        }
        if(ArrayUtils.isEmpty(approveUserIds)){
            throw new ResultException(ResultCode.SUBLIBRARY_FILE_APPROVEUSERS_NOT_FOUND_ERROR);
        }
        List<SublibraryFilesEntity> sublibraryFilesEntityList = new ArrayList<>();
        for(String id : sublibraryFileId){
            if(!hasSublibraryFileById(id)){
                throw new ResultException(ResultCode.SUBLIBRARY_FILE_ID_NOT_FOUND_ERROR);
            }
            SublibraryFilesEntity sublibraryFilesEntity = getSublibraryFileById(id);
            sublibraryFilesEntity.setProofreadUserSet(idsToSet(proofreadUserIds));
            sublibraryFilesEntity.setAuditUserSet(idsToSet(auditUserIds));
            if(auditMode != ApplicationConfig.SUBLIBRARY_FILE_AUDIT_NO_COUNTERSIGN){
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
        if(!userService.hasUserById(userId)){
            throw new ResultException(ResultCode.USER_ID_NOT_FOUND_ERROR);
        }
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

        SublibraryFilesAuditEntity sublibraryFilesAuditEntity = new SublibraryFilesAuditEntity();      // 审核详情

        /**
         *  子库文件流程控制：
         *  当前审核结果： pass --> 当前审核人：自己--> 报错，无通过权限
         *                                      其他人--> 设置子库文件进入下一模式：
         *                                                当前阶段为审核时-->  1)无会签：审核-->批准
         *                                                                     2)一人/多人会签：审核-->会签
         *                                                当前阶段为会签时-->  1)一人会签：会签-->批准（同上）
         *          *                                      @todo               2)多人会签：多人审核通过审核-->批准
         *                                                                                 若当前用户已会签过则报错，您已会签过
         *                                                当前阶段为批准时-->  修改子文件的通过状态为true
         *                 no   --> 停留当前模式   （--> 子库文件状态改为进行中）
         * */
        if(sublibraryFilesAuditRepository.existsBySublibraryFilesEntityAndUserEntityAndState(sublibraryFilesEntity,userEntity,sublibraryFilesEntityArgs.getState())){
            throw new ResultException(ResultCode.SUBLIBRARY_FILE_USER_ALREADY_COUNTERSIGN_ERROR);
        }

        if(sublibraryFilesAuditEntityArgs.isIfPass()){
            if(sublibraryFilesEntity.getUserEntity().getId().equals(userId)){    // 自己无权通过
                throw new ResultException(ResultCode.SUBLIBRARY_FILE_USER_PASS_DENIED_ERROR);
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
                // 若当前用户已会签过则报错，您已会签过
                if((sublibraryFilesEntity.getManyCounterSignState() + 1) != sublibraryFilesEntity.getCountersignUserSet().size()){
                    sublibraryFilesEntity.setManyCounterSignState(sublibraryFilesEntity.getManyCounterSignState() + 1);
                }else{                          // 所有人都已会签过
                    sublibraryFilesEntity.setState(sublibraryFilesEntityArgs.getState() + 1);
                }
                sublibraryFilesAuditEntity.setState(sublibraryFilesEntityArgs.getState());
            }else if(sublibraryFilesEntityArgs.getState() == ApplicationConfig.SUBLIBRARY_FILE_APPROVE){           // 当前为批准
                sublibraryFilesEntity.setIfApprove(true);                                                          // 子库文件通过审核
                sublibraryFilesAuditEntity.setState(sublibraryFilesEntityArgs.getState());
            }else{
                sublibraryFilesEntity.setState(sublibraryFilesEntityArgs.getState() + 1);
                sublibraryFilesAuditEntity.setState(sublibraryFilesEntityArgs.getState());
            }
        }else{                // 驳回
            if(sublibraryFilesEntityArgs.getState() == ApplicationConfig.SUBLIBRARY_FILE_COUNTERSIGN && sublibraryFilesEntityArgs.getAuditMode() == ApplicationConfig.SUBLIBRARY_FILE_AUDIT_MANY_COUNTERSIGN){
                // 当前为会签 且模式为多人会签
                // 若当前用户已会签过则报错，您已会签过
                if(sublibraryFilesAuditRepository.existsBySublibraryFilesEntityAndUserEntityAndState(sublibraryFilesEntity,userEntity,ApplicationConfig.SUBLIBRARY_FILE_COUNTERSIGN)){
                    throw new ResultException(ResultCode.SUBLIBRARY_FILE_USER_ALREADY_COUNTERSIGN_ERROR);
                }
            }
            sublibraryFilesAuditEntity.setState(sublibraryFilesEntityArgs.getState());

            sublibraryFilesEntity.setIfReject(true);           // 设置驳回状态为true
        }
        /**
         * 审核模式、审核阶段、审核结果、审核人、审核意见
         * */
        sublibraryFilesAuditEntity.setIfPass(sublibraryFilesAuditEntityArgs.isIfPass());               // 审核结果
        sublibraryFilesAuditEntity.setUserEntity(userEntity);                                          // 审核人
        sublibraryFilesAuditEntity.setSublibraryFilesEntity(sublibraryFilesEntity);                    // 审核详情所属子库文件
        sublibraryFilesAuditEntity.setAuditDescription(sublibraryFilesAuditEntityArgs.getAuditDescription());   // 审核意见
        sublibraryFilesAuditRepository.save(sublibraryFilesAuditEntity);


        return sublibraryFilesEntity;
    }

    // 申请二次修改
    public SublibraryFilesEntity applyForModify(String sublibraryFileId){
        SublibraryFilesEntity sublibraryFilesEntity = getSublibraryFileById(sublibraryFileId);

        return null;
    }

    // 驳回后  修改  id 修改方式 文件 版本（二次修改需要）
    public SublibraryFilesEntity modifySublibraryFile(String sublibraryFileId, int modifyWay, FileMetaEntity fileMetaEntity, String version){
        SublibraryFilesEntity sublibraryFilesEntity = getSublibraryFileById(sublibraryFileId);

        if(StringUtils.isEmpty(modifyWay)){
            throw new ResultException(ResultCode.SUBLIBRARY_FILE_MODIFYWAY_NOT_FOUND_ERROR);
        }
        if(modifyWay == ApplicationConfig.SUBLIBRARY_FILE_SECOND_MODIFY){        // 二次修改
            // TODO 判断文件是否通过二次修改申请
            if(StringUtils.isEmpty(version)){
                throw new ResultException(ResultCode.SUBLIBRARY_FILE_VERSION_NOT_FOUND_ERROR);
            }
        }


        return sublibraryFilesEntity;
    }
}
