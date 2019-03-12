package com.rengu.cosimulation.service;

import com.rengu.cosimulation.entity.*;
import com.rengu.cosimulation.enums.ResultCode;
import com.rengu.cosimulation.exception.ResultException;
import com.rengu.cosimulation.repository.SubtaskFilesRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
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

/**
 * Author: XYmar
 * Date: 2019/3/4 12:56
 */
@Service
@Slf4j
public class SubtaskFilesService {
    private final SubtaskService subtaskService;
    private final SubtaskFilesRepository subtaskFilesRepository;
    private final FileService fileService;
    @Autowired
    private UserService userService;

    @Autowired
    public SubtaskFilesService(FileService fileService, SubtaskFilesRepository subtaskFilesRepository, SubtaskService subtaskService) {
        this.fileService = fileService;
        this.subtaskFilesRepository = subtaskFilesRepository;
        this.subtaskService = subtaskService;
    }

    // 根据名称、后缀及子任务检查文件是否存在
    public boolean hasSubtaskFilesByNameAndExtensionAndSubtask(String name, String postfix, SubtaskEntity subTaskEntity) {
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(postfix)) {
            return false;
        }
        return subtaskFilesRepository.existsByNameAndPostfixAndSubTaskEntity(name, postfix, subTaskEntity);
    }

    // 根据名称、后缀及子任务查询文件
    public SubtaskFilesEntity getSubtaskFilesByNameAndPostfixAndSubtask(String name, String postfix, SubtaskEntity subTaskEntity) {
        return subtaskFilesRepository.findByNameAndPostfixAndSubTaskEntity(name, postfix, subTaskEntity).get();
    }

    // 根据子任务id创建文件
    @CacheEvict(value = "SubtaskFiles_Cache", allEntries = true)
    public List<SubtaskFilesEntity> saveSubtaskFilesByProDesignId(String subtaskId, List<FileMetaEntity> fileMetaEntityList) {
        if(!subtaskService.hasSubtaskById(subtaskId)){
            throw new ResultException(ResultCode.PRODESIGN_LINK_ID_NOT_FOUND_ERROR);
        }
        SubtaskEntity subTaskEntity = subtaskService.getSubtaskById(subtaskId);
        List<SubtaskFilesEntity> subtaskFilesEntityList = new ArrayList<>();
        for (FileMetaEntity fileMetaEntity : fileMetaEntityList) {
            String path = fileMetaEntity.getRelativePath().split("/")[1];

            // 判断该节点是否存在
            if (hasSubtaskFilesByNameAndExtensionAndSubtask(FilenameUtils.getBaseName(path), FilenameUtils.getExtension(path), subTaskEntity)) {
                SubtaskFilesEntity subtaskFilesEntity = getSubtaskFilesByNameAndPostfixAndSubtask(FilenameUtils.getBaseName(path), FilenameUtils.getExtension(path), subTaskEntity);
                subtaskFilesEntity.setCreateTime(new Date());
                subtaskFilesEntity.setName(FilenameUtils.getBaseName(fileMetaEntity.getRelativePath()));
                subtaskFilesEntity.setPostfix(FilenameUtils.getExtension(fileMetaEntity.getRelativePath()));
                subtaskFilesEntity.setType(fileMetaEntity.getType());
                subtaskFilesEntity.setSecretClass(fileMetaEntity.getSecretClass());
                subtaskFilesEntity.setCodeName(fileMetaEntity.getCodeName());
                subtaskFilesEntity.setVersion(1);
                subtaskFilesEntity.setFileEntity(fileService.getFileById(fileMetaEntity.getFileId()));
                subtaskFilesEntityList.add(subtaskFilesRepository.save(subtaskFilesEntity));
            } else {
                SubtaskFilesEntity subtaskFilesEntity = new SubtaskFilesEntity();
                subtaskFilesEntity.setName(StringUtils.isEmpty(FilenameUtils.getBaseName(fileMetaEntity.getRelativePath())) ? "-" : FilenameUtils.getBaseName(fileMetaEntity.getRelativePath()));
                subtaskFilesEntity.setPostfix(FilenameUtils.getExtension(fileMetaEntity.getRelativePath()));
                subtaskFilesEntity.setType(fileMetaEntity.getType());
                subtaskFilesEntity.setSecretClass(fileMetaEntity.getSecretClass());
                subtaskFilesEntity.setCodeName(fileMetaEntity.getCodeName());
                subtaskFilesEntity.setVersion(1);
                subtaskFilesEntity.setFileEntity(fileService.getFileById(fileMetaEntity.getFileId()));
                subtaskFilesEntity.setSubTaskEntity(subTaskEntity);
                subtaskFilesEntityList.add(subtaskFilesRepository.save(subtaskFilesEntity));
            }
        }
        return subtaskFilesEntityList;
    }

    // 根据子任务id查询子任务下的文件
    public List<SubtaskFilesEntity> getSubtaskFilesByProDesignId(String subtaskId) {
        if(!subtaskService.hasSubtaskById(subtaskId)){
            throw new ResultException(ResultCode.PRODESIGN_LINK_ID_NOT_FOUND_ERROR);
        }
        return subtaskFilesRepository.findBySubTaskEntity(subtaskService.getSubtaskById(subtaskId));
    }

    // 根据id查询子任务文件是否存在
    public boolean hasSubtaskFileById(String subtaskFileId) {
        if (StringUtils.isEmpty(subtaskFileId)) {
            return false;
        }
        return subtaskFilesRepository.existsById(subtaskFileId);
    }

    // 根据id查询子任务文件
    @Cacheable(value = "SubtaskFile_Cache", key = "#subtaskFileId")
    public SubtaskFilesEntity getSubtaskFileById(String subtaskFileId) {
        if (!hasSubtaskFileById(subtaskFileId)) {
            throw new ResultException(ResultCode.PRODESIGN_LINK_FILE_ID_NOT_FOUND_ERROR);
        }
        return subtaskFilesRepository.findById(subtaskFileId).get();
    }

    // 根据子任务文件的id下载文件
    public File exportSubtaskFileById(String subtaskFileId, String userId) throws IOException {
        if(!userService.hasUserById(userId)){
            throw new ResultException(ResultCode.USER_ID_NOT_FOUND_ERROR);
        }
        int userSecretClass = userService.getUserById(userId).getSecretClass();     //获取用户密级
        if(!hasSubtaskFileById(subtaskFileId)){
            throw new ResultException(ResultCode.PRODESIGN_LINK_FILE_ID_NOT_FOUND_ERROR);
        }
        int subtaskFileSecretClass = getSubtaskFileById(subtaskFileId).getSecretClass();
        // 用户只能下载小于等于自己密级的文件
        if(userSecretClass < subtaskFileSecretClass){
            throw new ResultException(ResultCode.PRODESIGN_LINK_FILE_DOWNLOAD_DENIED_ERROR);
        }
        SubtaskFilesEntity subtaskFilesEntity = getSubtaskFileById(subtaskFileId);
        File exportFile = new File(FileUtils.getTempDirectoryPath() + File.separator + subtaskFilesEntity.getName() + "." + subtaskFilesEntity.getFileEntity().getPostfix());
        FileUtils.copyFile(new File(subtaskFilesEntity.getFileEntity().getLocalPath()), exportFile);
        log.info(userService.getUserById(userId).getUsername() + "于" + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date()) + "下载了：" + subtaskFilesEntity.getName());
        return exportFile;
    }

    // 根据子任务文件id修改文件基本信息(类型、密级、代号)
    public SubtaskFilesEntity updateSubtaskFileId(String subtaskFileId, SubtaskFilesEntity subtaskFilesEntityArgs) {
        if(!hasSubtaskFileById(subtaskFileId)){
            throw new ResultException(ResultCode.PRODESIGN_LINK_FILE_ID_NOT_FOUND_ERROR);
        }
        SubtaskFilesEntity subtaskFilesEntity = getSubtaskFileById(subtaskFileId);
        if(subtaskFilesEntityArgs == null){
            throw new ResultException(ResultCode.PRODESIGN_LINK_FILE_ARGS_NOT_FOUND_ERROR);
        }
        if(!StringUtils.isEmpty(subtaskFilesEntityArgs.getType())){
            subtaskFilesEntity.setType(subtaskFilesEntityArgs.getType());
        }
        if(!StringUtils.isEmpty(subtaskFilesEntityArgs.getSecretClass())){
            subtaskFilesEntity.setSecretClass(subtaskFilesEntityArgs.getSecretClass());
        }
        if(!StringUtils.isEmpty(subtaskFilesEntityArgs.getCodeName())){
            subtaskFilesEntity.setCodeName(subtaskFilesEntityArgs.getCodeName());
        }
        return subtaskFilesRepository.save(subtaskFilesEntity);
    }

    // 根据子任务文件id删除文件
    public SubtaskFilesEntity deleteSubtaskFileId(String subtaskFileId) {
        if(!hasSubtaskFileById(subtaskFileId)){
            throw new ResultException(ResultCode.PRODESIGN_LINK_FILE_ID_NOT_FOUND_ERROR);
        }
        SubtaskFilesEntity subtaskFilesEntity = getSubtaskFileById(subtaskFileId);
        subtaskFilesRepository.delete(subtaskFilesEntity);
        return subtaskFilesEntity;
    }
}
