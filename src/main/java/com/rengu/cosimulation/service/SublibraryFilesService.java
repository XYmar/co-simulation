package com.rengu.cosimulation.service;

import com.rengu.cosimulation.entity.*;
import com.rengu.cosimulation.enums.ResultCode;
import com.rengu.cosimulation.exception.ResultException;
import com.rengu.cosimulation.repository.SublibraryFilesRepository;
import com.sun.deploy.util.ArrayUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Author: XYmar
 * Date: 2019/4/1 10:50
 */
@Service
public class SublibraryFilesService {
    private final SublibraryFilesRepository sublibraryFilesRepository;
    private final SublibraryService sublibraryService;
    private final FileService fileService;
    private final UserService userService;

    @Autowired
    public SublibraryFilesService(SublibraryFilesRepository sublibraryFilesRepository, FileService fileService, SublibraryService sublibraryService, UserService userService) {
        this.sublibraryFilesRepository = sublibraryFilesRepository;
        this.fileService = fileService;
        this.sublibraryService = sublibraryService;
        this.userService = userService;
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
    public List<SublibraryFilesEntity> saveSublibraryFilesBySublibraryId(String sublibraryId, List<FileMetaEntity> fileMetaEntityList) {
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
    // 选择文件的审核模式及四类审核人
    /*public SublibraryFilesEntity arrangeAudit(String sublibraryFileId, int mode, String[] proofreadUserIds, String[] auditUserIds, String[] countersignUserIds, String[] approveUserIds){
        if(StringUtils.isEmpty(sublibraryFileId)){
            throw new ResultException(ResultCode.SUBLIBRARY_FILE_ID_NOT_FOUND_ERROR);
        }
        SublibraryFilesEntity sublibraryFilesEntity = getSublibraryFileById(sublibraryFileId);
        for(){

        }

    }

    // 根据用户id数组，将用户转为set
    public Set<UserEntity> idsToSet(String[] ids){
        if(!ArrayUtils.isEmpty(ids)){

        }
    }*/
}
