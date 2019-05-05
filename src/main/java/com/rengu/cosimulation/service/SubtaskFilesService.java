package com.rengu.cosimulation.service;

import com.rengu.cosimulation.entity.*;
import com.rengu.cosimulation.enums.ResultCode;
import com.rengu.cosimulation.exception.ResultException;
import com.rengu.cosimulation.repository.DownloadLogsRepository;
import com.rengu.cosimulation.repository.SubtaskFilesHistoryRepository;
import com.rengu.cosimulation.repository.SubtaskFilesRepository;
import com.rengu.cosimulation.utils.ApplicationConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

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
    private final ProjectService projectService;
    private final UserService userService;
    private final SublibraryService sublibraryService;
    private final SubtaskFilesHistoryRepository subtaskFilesHistoryRepository;
    private final DownloadLogsRepository downloadLogsRepository;

    @Autowired
    public SubtaskFilesService(FileService fileService, SubtaskFilesRepository subtaskFilesRepository, SubtaskService subtaskService, ProjectService projectService, UserService userService, SublibraryService sublibraryService, SubtaskFilesHistoryRepository subtaskFilesHistoryRepository, DownloadLogsRepository downloadLogsRepository) {
        this.fileService = fileService;
        this.subtaskFilesRepository = subtaskFilesRepository;
        this.subtaskService = subtaskService;
        this.projectService = projectService;
        this.userService = userService;
        this.sublibraryService = sublibraryService;
        this.subtaskFilesHistoryRepository = subtaskFilesHistoryRepository;
        this.downloadLogsRepository = downloadLogsRepository;
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
    public List<SubtaskFilesEntity> saveSubtaskFilesByProDesignId(String subtaskId, String projectId, List<FileMetaEntity> fileMetaEntityList) {
        ProjectEntity projectEntity = projectService.getProjectById(projectId);
        SubtaskEntity subTaskEntity = subtaskService.getSubtaskById(subtaskId);
        if(subTaskEntity.getState() != ApplicationConfig.SUBTASK_START && !subTaskEntity.isIfModifyApprove()){                   // 上传文件前判断子任务是否已在进行中  或者二次修改申请被同意
            throw new ResultException(ResultCode.SUBTASK_HAVE_NOT_START);
        }
        List<SubtaskFilesEntity> subtaskFilesEntityList = new ArrayList<>();
        for (FileMetaEntity fileMetaEntity : fileMetaEntityList) {
            String path = fileMetaEntity.getRelativePath().split("/")[1];
            if(fileMetaEntity.getSecretClass() > projectEntity.getSecretClass()){  //文件密级只能低于等于该项目密级
                throw new ResultException(ResultCode.SUBTASK_FILE_SECRETCLASS_NOT_SUPPORT_ERROR);
            }

            if(StringUtils.isEmpty(fileMetaEntity.getSublibraryId())){
                throw new ResultException(ResultCode.SUBLIBRARY_NOT__APPOINT_ERROR);
            }
            SublibraryEntity sublibraryEntity = sublibraryService.getSublibraryById(fileMetaEntity.getSublibraryId());      // 子库
            // 判断该节点是否存在
            if (hasSubtaskFilesByNameAndExtensionAndSubtask(FilenameUtils.getBaseName(path), FilenameUtils.getExtension(path), subTaskEntity)) {
                SubtaskFilesEntity subtaskFilesEntity = getSubtaskFilesByNameAndPostfixAndSubtask(FilenameUtils.getBaseName(path), FilenameUtils.getExtension(path), subTaskEntity);
                subtaskFilesEntity.setCreateTime(new Date());
                subtaskFilesEntity.setName(FilenameUtils.getBaseName(fileMetaEntity.getRelativePath()));
                subtaskFilesEntity.setPostfix(FilenameUtils.getExtension(fileMetaEntity.getRelativePath()));
                subtaskFilesEntity.setType(fileMetaEntity.getType());
                subtaskFilesEntity.setSecretClass(fileMetaEntity.getSecretClass());
                subtaskFilesEntity.setProductNo(fileMetaEntity.getProductNo());
                subtaskFilesEntity.setFileNo(fileMetaEntity.getFileNo());
                subtaskFilesEntity.setVersion("M1");
                subtaskFilesEntity.setFileEntity(fileService.getFileById(fileMetaEntity.getFileId()));
                Set<SublibraryEntity> sublibraryEntities = subtaskFilesEntity.getSublibraryEntitySet();
                sublibraryEntities.add(sublibraryEntity);
                subtaskFilesEntity.setSublibraryEntitySet(sublibraryEntities);
                subtaskFilesEntityList.add(subtaskFilesRepository.save(subtaskFilesEntity));
            } else {
                SubtaskFilesEntity subtaskFilesEntity = new SubtaskFilesEntity();
                subtaskFilesEntity.setName(StringUtils.isEmpty(FilenameUtils.getBaseName(fileMetaEntity.getRelativePath())) ? "-" : FilenameUtils.getBaseName(fileMetaEntity.getRelativePath()));
                subtaskFilesEntity.setPostfix(FilenameUtils.getExtension(fileMetaEntity.getRelativePath()));
                subtaskFilesEntity.setType(fileMetaEntity.getType());
                subtaskFilesEntity.setSecretClass(fileMetaEntity.getSecretClass());
                subtaskFilesEntity.setProductNo(fileMetaEntity.getProductNo());
                subtaskFilesEntity.setFileNo(fileMetaEntity.getFileNo());
                subtaskFilesEntity.setVersion("M1");
                subtaskFilesEntity.setFileEntity(fileService.getFileById(fileMetaEntity.getFileId()));
                subtaskFilesEntity.setSubTaskEntity(subTaskEntity);
                Set<SublibraryEntity> sublibraryEntities = subtaskFilesEntity.getSublibraryEntitySet() == null ? new HashSet<>() : subtaskFilesEntity.getSublibraryEntitySet();
                sublibraryEntities.add(sublibraryEntity);
                subtaskFilesEntity.setSublibraryEntitySet(sublibraryEntities);
                subtaskFilesEntityList.add(subtaskFilesRepository.save(subtaskFilesEntity));
            }
        }
        return subtaskFilesEntityList;
    }

    // 驳回后  修改  [id 是否是直接修改 驳回修改内容是否提交到第一个流程（直接修改需要） 文件 版本（二次修改需要）]
    @CacheEvict(value = "SubtaskFiles_Cache", allEntries = true)
    public SubtaskFilesEntity modifySubtaskFiles(String subtaskFileId, FileMetaEntity fileMetaEntity) {
        SubtaskFilesEntity subtaskFilesEntity = getSubtaskFileById(subtaskFileId);
        SubtaskEntity subtaskEntity = subtaskFilesEntity.getSubTaskEntity();
        if(subtaskEntity.getState() >= ApplicationConfig.SUBTASK_TO_BE_AUDIT && subtaskEntity.getState() <= ApplicationConfig.SUBTASK_APPLY_FOR_MODIFY){              // 子任务审核中及审核后无权进行修改
            throw new ResultException(ResultCode.MODIFY_DENIED_ERROR);
        }
        if(StringUtils.isEmpty(fileMetaEntity.isIfDirectModify())){
            throw new ResultException(ResultCode.FILE_MODIFYWAY_NOT_FOUND_ERROR);
        }
        if(fileMetaEntity.getSecretClass() > subtaskFilesEntity.getSubTaskEntity().getProjectEntity().getSecretClass()){  //文件密级只能低于等于该项目密级
            throw new ResultException(ResultCode.SUBTASK_FILE_SECRETCLASS_NOT_SUPPORT_ERROR);
        }
        // 修改前存储此文件的备份 若备份已存在删除上一备份
        if(subtaskFilesHistoryRepository.existsByLeastSubtaskFilesEntityAndIfTemp(subtaskFilesEntity, true)){
            subtaskFilesHistoryRepository.delete(subtaskFilesHistoryRepository.findByLeastSubtaskFilesEntityAndIfTemp(subtaskFilesEntity, true).get(0));
        }
        saveSubtaskFilesHistoryBySubtaskFile(subtaskFilesEntity, true);

        if(!fileMetaEntity.isIfDirectModify()){            // 二次修改
            // 判断子任务是否通过二次修改申请
            if(!subtaskFilesEntity.getSubTaskEntity().isIfModifyApprove()){
                throw new ResultException(ResultCode.MODIFY_APPROVE_NOT_PASS_ERROR);
            }
            // 二次修改
            if(StringUtils.isEmpty(fileMetaEntity.getVersion())){
                throw new ResultException(ResultCode.FILE_VERSION_NOT_FOUND_ERROR);
            }
            // 修改前保存此文件历史
            saveSubtaskFilesHistoryBySubtaskFile(subtaskFilesEntity, false);
            subtaskFilesEntity.setVersion(fileMetaEntity.getVersion());
        }

        subtaskFilesEntity.setName(StringUtils.isEmpty(FilenameUtils.getBaseName(fileMetaEntity.getRelativePath())) ? "-" : FilenameUtils.getBaseName(fileMetaEntity.getRelativePath()));
        subtaskFilesEntity.setCreateTime(new Date());
        subtaskFilesEntity.setPostfix(FilenameUtils.getExtension(fileMetaEntity.getRelativePath()));
        subtaskFilesEntity.setType(fileMetaEntity.getType());
        subtaskFilesEntity.setSecretClass(fileMetaEntity.getSecretClass());
        subtaskFilesEntity.setProductNo(fileMetaEntity.getProductNo());
        subtaskFilesEntity.setFileNo(fileMetaEntity.getFileNo());
        subtaskFilesEntity.setFileEntity(fileService.getFileById(fileMetaEntity.getFileId()));
        SublibraryEntity sublibraryEntity = sublibraryService.getSublibraryById(fileMetaEntity.getSublibraryId());      // 所属子库
        Set<SublibraryEntity> sublibraryEntities = new HashSet<>();
        sublibraryEntities.add(sublibraryEntity);
        subtaskFilesEntity.setSublibraryEntitySet(sublibraryEntities);

        return subtaskFilesRepository.save(subtaskFilesEntity);
    }

    // 从子库文件生成子库文件历史
    public void saveSubtaskFilesHistoryBySubtaskFile(SubtaskFilesEntity sourceNode, boolean ifTemp) {
        SubtaskFilesHistoryEntity copyNode = new SubtaskFilesHistoryEntity();
        BeanUtils.copyProperties(sourceNode, copyNode, "id", "create_time", "leastSubtaskFilesEntity");
        copyNode.setLeastSubtaskFilesEntity(sourceNode);
        copyNode.setIfTemp(ifTemp);
        subtaskFilesHistoryRepository.save(copyNode);
    }

    // 从子库文件历史生成子库文件
    public void saveSubtaskFilesBySubtaskFile(SubtaskFilesEntity coverNode, SubtaskFilesHistoryEntity sourceNode) {
        BeanUtils.copyProperties(sourceNode, coverNode, "id", "create_time", "leastSubtaskFilesEntity", "ifDirectModify", "sublibraryEntitySet");
        subtaskFilesRepository.save(coverNode);
    }

    // 根据子任务id查询子任务下的文件
    public List<SubtaskFilesEntity> getSubtaskFilesBySubtaskId(String subtaskId) {
        if(!subtaskService.hasSubtaskById(subtaskId)){
            throw new ResultException(ResultCode.SUBTASK_ID_NOT_FOUND_ERROR);
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
            throw new ResultException(ResultCode.SUBTASK_FILE_ID_NOT_FOUND_ERROR);
        }
        return subtaskFilesRepository.findById(subtaskFileId).get();
    }

    // 根据子任务文件的id下载文件
    public File exportSubtaskFileById(String subtaskFileId, String userId) throws IOException {
        if(!userService.hasUserById(userId)){
            throw new ResultException(ResultCode.USER_ID_NOT_FOUND_ERROR);
        }
        UserEntity userEntity = userService.getUserById(userId);
        int userSecretClass = userEntity.getSecretClass();     //获取用户密级
        if(!hasSubtaskFileById(subtaskFileId)){
            throw new ResultException(ResultCode.SUBTASK_FILE_ID_NOT_FOUND_ERROR);
        }
        int subtaskFileSecretClass = getSubtaskFileById(subtaskFileId).getSecretClass();
        // 用户只能下载小于等于自己密级的文件
        if(userSecretClass < subtaskFileSecretClass){
            throw new ResultException(ResultCode.SUBTASK_FILE_DOWNLOAD_DENIED_ERROR);
        }
        SubtaskFilesEntity subtaskFilesEntity = getSubtaskFileById(subtaskFileId);
        File exportFile = new File(FileUtils.getTempDirectoryPath() + File.separator + subtaskFilesEntity.getName() + "." + subtaskFilesEntity.getFileEntity().getPostfix());
        FileUtils.copyFile(new File(subtaskFilesEntity.getFileEntity().getLocalPath()), exportFile);
        log.info(userService.getUserById(userId).getUsername() + "于" + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date()) + "下载了：" + subtaskFilesEntity.getName());
        DownloadLogsEntity downloadLogsEntity = new DownloadLogsEntity();
        downloadLogsEntity.setFileName(subtaskFilesEntity.getName());
        downloadLogsEntity.setUserEntity(userEntity);
        downloadLogsEntity.setFileEntity(subtaskFilesEntity.getFileEntity());
        downloadLogsRepository.save(downloadLogsEntity);
        return exportFile;
    }

    // 根据子任务文件id修改文件基本信息(类型、密级、代号、所属库)
    public SubtaskFilesEntity updateSubtaskFileId(String subtaskFileId, SubtaskFilesEntity subtaskFilesEntityArgs, String sublibraryId) {
        if(!hasSubtaskFileById(subtaskFileId)){
            throw new ResultException(ResultCode.SUBTASK_FILE_ID_NOT_FOUND_ERROR);
        }
        SubtaskFilesEntity subtaskFilesEntity = getSubtaskFileById(subtaskFileId);
        if(subtaskFilesEntityArgs == null){
            throw new ResultException(ResultCode.SUBTASK_FILE_ARGS_NOT_FOUND_ERROR);
        }
        if(!StringUtils.isEmpty(subtaskFilesEntityArgs.getType())){
            subtaskFilesEntity.setType(subtaskFilesEntityArgs.getType());
        }
        if(!StringUtils.isEmpty(subtaskFilesEntityArgs.getSecretClass())){
            subtaskFilesEntity.setSecretClass(subtaskFilesEntityArgs.getSecretClass());
        }
        if(!StringUtils.isEmpty(subtaskFilesEntityArgs.getProductNo())){
            subtaskFilesEntity.setProductNo(subtaskFilesEntityArgs.getProductNo());
        }
        if(!StringUtils.isEmpty(subtaskFilesEntityArgs.getFileNo())){
            subtaskFilesEntity.setFileNo(subtaskFilesEntityArgs.getFileNo());
        }
        if(!StringUtils.isEmpty(sublibraryId)){
            SublibraryEntity sublibraryEntity = sublibraryService.getSublibraryById(sublibraryId);      // 子库

            Set<SublibraryEntity> sublibraryEntities = new HashSet<>();         // 修改所属库为一个
            sublibraryEntities.add(sublibraryEntity);
            subtaskFilesEntity.setSublibraryEntitySet(sublibraryEntities);
        }

        return subtaskFilesRepository.save(subtaskFilesEntity);
    }

    // 根据子任务文件id删除文件
    public SubtaskFilesEntity deleteSubtaskFileId(String subtaskFileId) {
        if(!hasSubtaskFileById(subtaskFileId)){
            throw new ResultException(ResultCode.SUBTASK_FILE_ID_NOT_FOUND_ERROR);
        }
        SubtaskFilesEntity subtaskFilesEntity = getSubtaskFileById(subtaskFileId);
        subtaskFilesRepository.delete(subtaskFilesEntity);
        return subtaskFilesEntity;
    }

    // 根据子库文件id查询是否有可撤销文件(临时文件)
    public boolean ifHasTemp(String subtaskFileId){
        return subtaskFilesHistoryRepository.existsByLeastSubtaskFilesEntityAndIfTemp(getSubtaskFileById(subtaskFileId), true);
    }

    // 根据子库文件id查找其历史版本文件
    public List<SubtaskFilesHistoryEntity> getSubtaskHistoriesFiles(String subtaskFileId){
        return subtaskFilesHistoryRepository.findByLeastSubtaskFilesEntityAndIfTemp(getSubtaskFileById(subtaskFileId), false);
    }

    // 撤销文件操作    只支持一次撤销操作，撤销回上一步  要再次撤销需再次修改
    public SubtaskFilesEntity revokeModify(String subtaskFileId){
        if(!ifHasTemp(subtaskFileId)){       // 当前文件不存在可撤销的文件
            throw new ResultException(ResultCode.FILE_HAS_NO_REVOKE_FILE);
        }
        // 当前文件
        SubtaskFilesEntity subtaskFilesEntity = getSubtaskFileById(subtaskFileId);
        // 需撤销至的文件(该文件的临时文件)
        SubtaskFilesHistoryEntity sourceNode = subtaskFilesHistoryRepository.findByLeastSubtaskFilesEntityAndIfTemp(subtaskFilesEntity, true).get(0);
        // 历史版本提为当前文件
        saveSubtaskFilesBySubtaskFile(subtaskFilesEntity, sourceNode);
        subtaskFilesHistoryRepository.delete(sourceNode);

        return getSubtaskFileById(subtaskFileId);
    }

    // 更换版本（二次修改可恢复其中任意版本）   版本为空则为直接修改，否则为二次修改，切换到指定版本
    public SubtaskFilesEntity versionReplace(String subtaskFileId, String version){
        // 当前文件
        SubtaskFilesEntity subtaskFilesEntity = getSubtaskFileById(subtaskFileId);
        // 需撤销至的文件
        SubtaskFilesHistoryEntity sourceNode = subtaskFilesHistoryRepository.findByLeastSubtaskFilesEntityAndIfTempAndVersion(subtaskFilesEntity, false, version);
        // 历史版本提为当前文件
        saveSubtaskFilesBySubtaskFile(subtaskFilesEntity, sourceNode);
        subtaskFilesHistoryRepository.delete(sourceNode);

        return getSubtaskFileById(subtaskFileId);
    }
}
