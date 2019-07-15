package com.rengu.cosimulation.service;

import com.rengu.cosimulation.entity.*;
import com.rengu.cosimulation.enums.ResultCode;
import com.rengu.cosimulation.exception.ResultException;
import com.rengu.cosimulation.repository.DownloadLogsRepository;
import com.rengu.cosimulation.repository.SublibraryFilesHistoryRepository;
import com.rengu.cosimulation.repository.SubtaskFilesHistoryRepository;
import com.rengu.cosimulation.repository.SubtaskFilesRepository;
import com.rengu.cosimulation.utils.ApplicationConfig;
import lombok.extern.slf4j.Slf4j;
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
    private final SublibraryFilesHistoryRepository sublibraryFilesHistoryRepository;

    @Autowired
    public SubtaskFilesService(FileService fileService, SubtaskFilesRepository subtaskFilesRepository, SubtaskService subtaskService, ProjectService projectService, UserService userService, SublibraryService sublibraryService, SubtaskFilesHistoryRepository subtaskFilesHistoryRepository, DownloadLogsRepository downloadLogsRepository, SublibraryFilesHistoryRepository sublibraryFilesHistoryRepository) {
        this.fileService = fileService;
        this.subtaskFilesRepository = subtaskFilesRepository;
        this.subtaskService = subtaskService;
        this.projectService = projectService;
        this.userService = userService;
        this.sublibraryService = sublibraryService;
        this.subtaskFilesHistoryRepository = subtaskFilesHistoryRepository;
        this.downloadLogsRepository = downloadLogsRepository;
        this.sublibraryFilesHistoryRepository = sublibraryFilesHistoryRepository;
    }

    // 根据名称、后缀及子任务检查文件是否存在
    public boolean hasSubtaskFilesByNameAndExtensionAndSubtask(String name, String postfix, Subtask subTask) {
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(postfix)) {
            return false;
        }
        return subtaskFilesRepository.existsByNameAndPostfixAndSubtask(name, postfix, subTask);
    }

    // 根据名称、后缀及子任务查询文件
    public SubtaskFile getSubtaskFilesByNameAndPostfixAndSubtask(String name, String postfix, Subtask subTask) {
        return subtaskFilesRepository.findByNameAndPostfixAndSubtask(name, postfix, subTask).get();
    }

    // 根据子任务id创建文件
    @CacheEvict(value = "SubtaskFiles_Cache", allEntries = true)
    public List<SubtaskFile> saveSubtaskFilesByProDesignId(String subtaskId, String projectId, List<FileMeta> fileMetaList) {
        Project project = projectService.getProjectById(projectId);
        Subtask subTask = subtaskService.getSubtaskById(subtaskId);
        if(subTask.getState() != ApplicationConfig.SUBTASK_START && !subTask.isIfModifyApprove()){                   // 上传文件前判断子任务是否已在进行中  或者二次修改申请被同意
            throw new ResultException(ResultCode.SUBTASK_HAVE_NOT_START);
        }
        List<SubtaskFile> subtaskFileList = new ArrayList<>();
        for (FileMeta fileMeta : fileMetaList) {
            String path = fileMeta.getRelativePath().split("/")[1];
            if(fileMeta.getSecretClass() > project.getSecretClass()){  //文件密级只能低于等于该项目密级
                throw new ResultException(ResultCode.SUBTASK_FILE_SECRETCLASS_NOT_SUPPORT_ERROR);
            }

            if(StringUtils.isEmpty(fileMeta.getSublibraryId())){
                throw new ResultException(ResultCode.SUBLIBRARY_NOT__APPOINT_ERROR);
            }
            SubDepot subDepot = sublibraryService.getSublibraryById(fileMeta.getSublibraryId());      // 子库
            // 上传文件，前端会先调用判断接口，此处无需判断
            SubtaskFile subtaskFile = new SubtaskFile();
            subtaskFile.setName(StringUtils.isEmpty(FilenameUtils.getBaseName(fileMeta.getRelativePath())) ? "-" : FilenameUtils.getBaseName(fileMeta.getRelativePath()));
            subtaskFile.setPostfix(FilenameUtils.getExtension(fileMeta.getRelativePath()));
            subtaskFile.setType(fileMeta.getType());
            subtaskFile.setSecretClass(fileMeta.getSecretClass());
            subtaskFile.setProductNo(fileMeta.getProductNo());
            subtaskFile.setFileNo(fileMeta.getFileNo());
            if(!StringUtils.isEmpty(fileMeta.getVersion())){
                subtaskFile.setVersion(fileMeta.getVersion());
            }else {
                subtaskFile.setVersion("M1");
            }
            subtaskFile.setFiles(fileService.getFileById(fileMeta.getFileId()));
            subtaskFile.setSubtask(subTask);
            Set<SubDepot> sublibraryEntities = subtaskFile.getSubDepotSet() == null ? new HashSet<>() : subtaskFile.getSubDepotSet();
            sublibraryEntities.add(subDepot);
            subtaskFile.setSubDepotSet(sublibraryEntities);
            subtaskFileList.add(subtaskFilesRepository.save(subtaskFile));
        }
        return subtaskFileList;
    }

    // 上传前判断哪些已存在
    public List<SubtaskFile> findExistSubtaskFiles(String subtaskId, List<FileMeta> fileMetaList) {
        Subtask subTask = subtaskService.getSubtaskById(subtaskId);

        List<SubtaskFile> subtaskFileList = new ArrayList<>();
        for (FileMeta fileMeta : fileMetaList) {
            String name = FilenameUtils.getBaseName(fileMeta.getRelativePath());
            String fileNo = fileMeta.getFileNo();
            String productNo = fileMeta.getProductNo();
            String postfix = FilenameUtils.getExtension(fileMeta.getRelativePath());
            int secretClass = fileMeta.getSecretClass();
            String version;
            if(!StringUtils.isEmpty(fileMeta.getVersion())){
                version = fileMeta.getVersion();
            }else {
                version = "M1";
            }
            String type = fileMeta.getType();

            SubtaskFile subtaskFile = subtaskFilesRepository.findByNameAndFileNoAndProductNoAndPostfixAndSecretClassAndSubtaskAndVersionAndType(name, fileNo, productNo, postfix, secretClass, subTask, version, type);
            if(!StringUtils.isEmpty(subtaskFile)){
                subtaskFileList.add(subtaskFile);
            }
        }
        return subtaskFileList;
    }


    // 驳回后  修改  [id 是否是直接修改 驳回修改内容是否提交到第一个流程（直接修改需要） 文件 版本（二次修改需要）]
    @CacheEvict(value = "SubtaskFiles_Cache", allEntries = true)
    public SubtaskFile modifySubtaskFiles(String subtaskFileId, FileMeta fileMeta) {
        SubtaskFile subtaskFile = getSubtaskFileById(subtaskFileId);
        Subtask subtask = subtaskFile.getSubtask();
        if(StringUtils.isEmpty(fileMeta.isIfDirectModify())){
            throw new ResultException(ResultCode.FILE_MODIFYWAY_NOT_FOUND_ERROR);
        }
        if(fileMeta.isIfDirectModify() && !(subtask.getState() == ApplicationConfig.SUBTASK_AUDIT_OVER && subtask.isIfReject())){              // 只有子任务审核结束并且被驳回才能直接修改
            throw new ResultException(ResultCode.MODIFY_DENIED_ERROR);
        }
        if(!fileMeta.isIfDirectModify() && (subtask.getState() != ApplicationConfig.SUBTASK_APPLY_FOR_MODIFY_APPROVE)){              // 只有通过二次修改申请才能进行二次修改
            throw new ResultException(ResultCode.MODIFY_DENIED_ERROR);
        }

        if(fileMeta.getSecretClass() > subtaskFile.getSubtask().getProject().getSecretClass()){  //文件密级只能低于等于该项目密级
            throw new ResultException(ResultCode.SUBTASK_FILE_SECRETCLASS_NOT_SUPPORT_ERROR);
        }
        // 修改前存储此文件的备份 若备份已存在删除上一备份
        if(subtaskFilesHistoryRepository.existsByLeastSubtaskFileAndIfTemp(subtaskFile, true)){
            subtaskFilesHistoryRepository.delete(subtaskFilesHistoryRepository.findByLeastSubtaskFileAndIfTemp(subtaskFile, true).get(0));
        }
        saveSubtaskFilesHistoryBySubtaskFile(subtaskFile, true);

        if(!fileMeta.isIfDirectModify()){            // 二次修改
            // 判断子任务是否通过二次修改申请
            if(!subtaskFile.getSubtask().isIfModifyApprove()){
                throw new ResultException(ResultCode.MODIFY_APPROVE_NOT_PASS_ERROR);
            }
            // 二次修改
            // 修改前保存此文件历史
            saveSubtaskFilesHistoryBySubtaskFile(subtaskFile, false);
            subtaskFile.setVersion(subtask.getVersion());
        }

        subtaskFile.setName(StringUtils.isEmpty(FilenameUtils.getBaseName(fileMeta.getRelativePath())) ? "-" : FilenameUtils.getBaseName(fileMeta.getRelativePath()));
        subtaskFile.setCreateTime(new Date());
        subtaskFile.setPostfix(FilenameUtils.getExtension(fileMeta.getRelativePath()));
        subtaskFile.setType(fileMeta.getType());
        subtaskFile.setSecretClass(fileMeta.getSecretClass());
        subtaskFile.setProductNo(fileMeta.getProductNo());
        subtaskFile.setFileNo(fileMeta.getFileNo());
        subtaskFile.setFiles(fileService.getFileById(fileMeta.getFileId()));
        SubDepot subDepot = sublibraryService.getSublibraryById(fileMeta.getSublibraryId());      // 所属子库
        Set<SubDepot> sublibraryEntities = new HashSet<>();
        sublibraryEntities.add(subDepot);
        subtaskFile.setSubDepotSet(sublibraryEntities);

        return subtaskFilesRepository.save(subtaskFile);
    }

    // 批量删除
    public List<SubtaskFile> deleteFilesInBatch(String[] ids) {
        List<SubtaskFile> subtaskFileList = new ArrayList<>();
        for (String id : ids) {
            SubtaskFile subtaskFile = getSubtaskFileById(id);
            subtaskFileList.add(subtaskFile);
            List<SubtaskFileHis> sublibraryFilesHistoryEntityList = subtaskFilesHistoryRepository.findByLeastSubtaskFile(subtaskFile);
            subtaskFilesHistoryRepository.deleteInBatch(sublibraryFilesHistoryEntityList);
        }
        subtaskFilesRepository.deleteInBatch(subtaskFileList);
        return subtaskFileList;
    }

    // 从子库文件生成子库文件历史
    public void saveSubtaskFilesHistoryBySubtaskFile(SubtaskFile sourceNode, boolean ifTemp) {
        SubtaskFileHis copyNode = new SubtaskFileHis();
        BeanUtils.copyProperties(sourceNode, copyNode, "id", "create_time", "leastSubtaskFile");
        copyNode.setLeastSubtaskFile(sourceNode);
        copyNode.setIfTemp(ifTemp);
        subtaskFilesHistoryRepository.save(copyNode);
    }

    // 从子库文件历史生成子库文件
    public void saveSubtaskFilesBySubtaskFile(SubtaskFile coverNode, SubtaskFileHis sourceNode) {
        BeanUtils.copyProperties(sourceNode, coverNode, "id", "create_time", "leastSubtaskFile", "ifDirectModify", "subDepotSet");
        subtaskFilesRepository.save(coverNode);
    }

    // 根据子任务id查询子任务下的文件
    public List<SubtaskFile> getSubtaskFilesBySubtaskId(String subtaskId) {
        if(!subtaskService.hasSubtaskById(subtaskId)){
            throw new ResultException(ResultCode.SUBTASK_ID_NOT_FOUND_ERROR);
        }
        return subtaskFilesRepository.findBySubtask(subtaskService.getSubtaskById(subtaskId));
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
    public SubtaskFile getSubtaskFileById(String subtaskFileId) {
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
        Users users = userService.getUserById(userId);
        int userSecretClass = users.getSecretClass();     //获取用户密级
        if(!hasSubtaskFileById(subtaskFileId)){
            throw new ResultException(ResultCode.SUBTASK_FILE_ID_NOT_FOUND_ERROR);
        }
        int subtaskFileSecretClass = getSubtaskFileById(subtaskFileId).getSecretClass();
        // 用户只能下载小于等于自己密级的文件
        if(userSecretClass < subtaskFileSecretClass){
            throw new ResultException(ResultCode.SUBTASK_FILE_DOWNLOAD_DENIED_ERROR);
        }
        SubtaskFile subtaskFile = getSubtaskFileById(subtaskFileId);
        File exportFile = new File(FileUtils.getTempDirectoryPath() + File.separator + subtaskFile.getName() + "." + subtaskFile.getFiles().getPostfix());
        FileUtils.copyFile(new File(subtaskFile.getFiles().getLocalPath()), exportFile);
        log.info(userService.getUserById(userId).getUsername() + "于" + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date()) + "下载了：" + subtaskFile.getName());
        DownloadLog downloadLog = new DownloadLog();
        downloadLog.setFileName(subtaskFile.getName());
        downloadLog.setUsers(users);
        downloadLog.setFiles(subtaskFile.getFiles());
        downloadLogsRepository.save(downloadLog);
        return exportFile;
    }

    // 根据子任务文件id修改文件基本信息(类型、密级、代号、所属库)
    public SubtaskFile updateSubtaskFileId(String subtaskFileId, SubtaskFile subtaskFileArgs, String sublibraryId) {
        if(!hasSubtaskFileById(subtaskFileId)){
            throw new ResultException(ResultCode.SUBTASK_FILE_ID_NOT_FOUND_ERROR);
        }
        SubtaskFile subtaskFile = getSubtaskFileById(subtaskFileId);
        if(subtaskFileArgs == null){
            throw new ResultException(ResultCode.SUBTASK_FILE_ARGS_NOT_FOUND_ERROR);
        }
        if(!StringUtils.isEmpty(subtaskFileArgs.getType())){
            subtaskFile.setType(subtaskFileArgs.getType());
        }
        if(!StringUtils.isEmpty(subtaskFileArgs.getSecretClass())){
            subtaskFile.setSecretClass(subtaskFileArgs.getSecretClass());
        }
        if(!StringUtils.isEmpty(subtaskFileArgs.getProductNo())){
            subtaskFile.setProductNo(subtaskFileArgs.getProductNo());
        }
        if(!StringUtils.isEmpty(subtaskFileArgs.getFileNo())){
            subtaskFile.setFileNo(subtaskFileArgs.getFileNo());
        }
        if(!StringUtils.isEmpty(sublibraryId)){
            SubDepot subDepot = sublibraryService.getSublibraryById(sublibraryId);      // 子库

            Set<SubDepot> sublibraryEntities = new HashSet<>();         // 修改所属库为一个
            sublibraryEntities.add(subDepot);
            subtaskFile.setSubDepotSet(sublibraryEntities);
        }

        return subtaskFilesRepository.save(subtaskFile);
    }

    // 根据子任务文件id删除文件
    public SubtaskFile deleteSubtaskFileId(String subtaskFileId) {
        if(!hasSubtaskFileById(subtaskFileId)){
            throw new ResultException(ResultCode.SUBTASK_FILE_ID_NOT_FOUND_ERROR);
        }
        SubtaskFile subtaskFile = getSubtaskFileById(subtaskFileId);
        List<SubtaskFileHis> sublibraryFilesHistoryEntityList = subtaskFilesHistoryRepository.findByLeastSubtaskFile(subtaskFile);
        subtaskFilesHistoryRepository.deleteInBatch(sublibraryFilesHistoryEntityList);
        subtaskFilesRepository.delete(subtaskFile);
        return subtaskFile;
    }

    // 根据子库文件id查询是否有可撤销文件(临时文件)
    public boolean ifHasTemp(String subtaskFileId){
        return subtaskFilesHistoryRepository.existsByLeastSubtaskFileAndIfTemp(getSubtaskFileById(subtaskFileId), true);
    }

    // 根据子库文件id查找其历史版本文件
    public List<SubtaskFileHis> getSubtaskHistoriesFiles(String subtaskFileId){
        return subtaskFilesHistoryRepository.findByLeastSubtaskFileAndIfTemp(getSubtaskFileById(subtaskFileId), false);
    }

    // 撤销文件操作    只支持一次撤销操作，撤销回上一步  要再次撤销需再次修改
    public SubtaskFile revokeModify(String subtaskFileId){
        if(!ifHasTemp(subtaskFileId)){       // 当前文件不存在可撤销的文件
            throw new ResultException(ResultCode.FILE_HAS_NO_REVOKE_FILE);
        }
        // 当前文件
        SubtaskFile subtaskFile = getSubtaskFileById(subtaskFileId);
        // 需撤销至的文件(该文件的临时文件)
        SubtaskFileHis sourceNode = subtaskFilesHistoryRepository.findByLeastSubtaskFileAndIfTemp(subtaskFile, true).get(0);
        // 历史版本提为当前文件
        saveSubtaskFilesBySubtaskFile(subtaskFile, sourceNode);
        subtaskFilesHistoryRepository.delete(sourceNode);

        SubtaskFile subtaskFile1 = getSubtaskFileById(subtaskFileId);
        subtaskFile1.setVersion(subtaskFile.getVersion());
        return subtaskFilesRepository.save(subtaskFile1);
    }

    // 更换版本（二次修改可恢复其中任意版本）   版本为空则为直接修改，否则为二次修改，切换到指定版本
    public SubtaskFile versionReplace(String subtaskFileId, String version){
        // 当前文件
        SubtaskFile subtaskFile = getSubtaskFileById(subtaskFileId);
        // 需撤销至的文件
        SubtaskFileHis sourceNode = subtaskFilesHistoryRepository.findByLeastSubtaskFileAndIfTempAndVersion(subtaskFile, false, version);
        // 历史版本提为当前文件
        saveSubtaskFilesBySubtaskFile(subtaskFile, sourceNode);
        subtaskFilesHistoryRepository.delete(sourceNode);

        return getSubtaskFileById(subtaskFileId);
    }
}
