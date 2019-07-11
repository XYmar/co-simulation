package com.rengu.cosimulation.service;

import com.rengu.cosimulation.entity.*;
import com.rengu.cosimulation.enums.ResultCode;
import com.rengu.cosimulation.exception.ResultException;
import com.rengu.cosimulation.repository.*;
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
    private final DownloadLogsRepository downloadLogsRepository;
    private final LibraryService libraryService;
    private final RoleService roleService;

    @Autowired
    public SublibraryFilesService(SublibraryFilesRepository sublibraryFilesRepository, FileService fileService, SublibraryService sublibraryService, UserService userService, SublibraryFilesAuditRepository sublibraryFilesAuditRepository, SublibraryFilesHistoryRepository sublibraryFilesHistoryRepository, SubtaskFilesRepository subtaskFilesRepository, DownloadLogsRepository downloadLogsRepository, LibraryService libraryService, RoleService roleService) {
        this.sublibraryFilesRepository = sublibraryFilesRepository;
        this.fileService = fileService;
        this.sublibraryService = sublibraryService;
        this.userService = userService;
        this.sublibraryFilesAuditRepository = sublibraryFilesAuditRepository;
        this.sublibraryFilesHistoryRepository = sublibraryFilesHistoryRepository;
        this.subtaskFilesRepository = subtaskFilesRepository;
        this.downloadLogsRepository = downloadLogsRepository;
        this.libraryService = libraryService;
        this.roleService = roleService;
    }

    // 根据名称、后缀及子库检查文件是否存在
    public boolean hasSublibraryFilesByNameAndExtensionAndSublibrary(String name, String postfix, SubDepot subDepot) {
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(postfix)) {
            return false;
        }
        return sublibraryFilesRepository.existsByNameAndPostfixAndSubDepot(name, postfix, subDepot);
    }

    // 根据名称、后缀及子库查询文件
    public SubDepotFile getSublibraryFilesByNameAndPostfixAndSublibrary(String name, String postfix, SubDepot subDepot) {
        return sublibraryFilesRepository.findByNameAndPostfixAndSubDepot(name, postfix, subDepot).get();
    }

    /**
     * 子库文件
     *     上传：
     *          子库文件直接上传不会出现此问题
     *     子任务入库：
     *          入库时先根据所有字段查询文件是否存在
     *              不存在 --> 直接入库
     *              存在   --> 判断版本是否有变化
     *                             有变化 --> 已有文件存入历史文件，并更新当前文件
     *                             无变化 --> 更新当前文件
     *
     * 根据子库文件查看其历史文件
     * */

    // 根据子库id创建文件 （后台不判断节点是否存在）
    @CacheEvict(value = "SublibraryFiles_Cache", allEntries = true)
    public List<SubDepotFile> saveSublibraryFilesBySublibraryId(String sublibraryId, String userId, List<FileMeta> fileMetaList) {
        SubDepot subDepot = sublibraryService.getSublibraryById(sublibraryId);
        Users users = userService.getUserById(userId);
        List<SubDepotFile> subDepotFileList = new ArrayList<>();
        for (FileMeta fileMeta : fileMetaList) {
            SubDepotFile subDepotFile = new SubDepotFile();
            subDepotFile.setName(StringUtils.isEmpty(FilenameUtils.getBaseName(fileMeta.getRelativePath())) ? "-" : FilenameUtils.getBaseName(fileMeta.getRelativePath()));
            subDepotFile.setPostfix(FilenameUtils.getExtension(fileMeta.getRelativePath()));
            subDepotFile.setType(fileMeta.getType());
            if(fileMeta.getSecretClass() > users.getSecretClass()){
                throw new ResultException(ResultCode.SUBLIBRARY_FILE_UPLOAD_DENIED);
            }
            subDepotFile.setSecretClass(fileMeta.getSecretClass());
            subDepotFile.setProductNo(fileMeta.getProductNo());
            subDepotFile.setFileNo(fileMeta.getFileNo());
            subDepotFile.setVersion("M1");
            // 参数库： 系统管理员默认上传第一版，置为已审核通过状态
            if(users.getRoleEntities().contains(roleService.getRoleByName(ApplicationConfig.DEFAULT_ADMIN_ROLE_NAME))){
                subDepotFile.setIfApprove(true);
                subDepotFile.setState(ApplicationConfig.SUBLIBRARY_FILE_AUDIT_OVER);
            }else {
                subDepotFile.setIfApprove(false);
                subDepotFile.setState(0);
            }
            subDepotFile.setIfReject(false);
            subDepotFile.setManyCounterSignState(0);           // 多人会签模式，此时无人开始会签
            subDepotFile.setUsers(users);
            subDepotFile.setFiles(fileService.getFileById(fileMeta.getFileId()));
            subDepotFile.setSubDepot(subDepot);
            subDepotFileList.add(sublibraryFilesRepository.save(subDepotFile));
        }
        return subDepotFileList;
    }

    // 上传前判断哪些已存在
    public List<SubDepotFile> findExistSubDepotFiles(String sublibraryId, String userId, List<FileMeta> fileMetaList) {
        SubDepot subDepot = sublibraryService.getSublibraryById(sublibraryId);
        Users users = userService.getUserById(userId);
        List<SubDepotFile> subDepotFileList = new ArrayList<>();

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

            SubDepotFile subDepotFile = sublibraryFilesRepository.findByNameAndFileNoAndProductNoAndPostfixAndSecretClassAndSubDepotAndTypeAndUsersAndVersion(name, fileNo, productNo, postfix, secretClass, subDepot, type, users, version);
            if(!StringUtils.isEmpty(subDepotFile)){
                subDepotFileList.add(subDepotFile);
            }
        }
        return subDepotFileList;
    }

    // 子任务入库
    public void stockIn(Subtask subtask){
        List<SubtaskFile> subtaskFileList = subtaskFilesRepository.findBySubtask(subtask);    // 子任务下的文件
        List<SubDepotFile> subDepotFileList = new ArrayList<>();
        for(SubtaskFile subtaskFile : subtaskFileList){
            // 子任务文件属于几个子库
            Set<SubDepot> subDepotSet = subtaskFile.getSubDepotSet();
            for(SubDepot subDepot : subDepotSet){
                SubDepotFile subDepotFile;
                // 判断此子任务文件在库中是否存在再入库
                if(sublibraryFilesRepository.existsBySubtaskFileId(subtaskFile.getId())){
                    subDepotFile = sublibraryFilesRepository.findBySubtaskFileId(subtaskFile.getId());
                    if(!subtaskFile.getVersion().equals(subDepotFile.getVersion())){
                        // 版本有变化，存入历史，更改此文件为新文件
                        saveSublibraryFilesHistoryBySublibraryFile(subDepotFile, false);
                    }
                }else {  // 入库文件在库中不存在
                    subDepotFile = new SubDepotFile();
                }
                subDepotFile.setSubtaskFileId(subtaskFile.getId());
                subDepotFile.setCreateTime(new Date());
                subDepotFile.setName(subtaskFile.getName());
                subDepotFile.setPostfix(subtaskFile.getPostfix());
                subDepotFile.setType(subtaskFile.getType());
                subDepotFile.setSecretClass(subtaskFile.getSecretClass());
                subDepotFile.setProductNo(subtaskFile.getProductNo());
                subDepotFile.setFileNo(subtaskFile.getFileNo());
                subDepotFile.setVersion(subtaskFile.getVersion());
                subDepotFile.setIfApprove(true);
                subDepotFile.setIfReject(false);
                subDepotFile.setState(ApplicationConfig.SUBLIBRARY_FILE_AUDIT_OVER);
                subDepotFile.setManyCounterSignState(0);
                subDepotFile.setAuditMode(0);
                subDepotFile.setIfModifyApprove(false);
                subDepotFile.setRejectState(0);
                subDepotFile.setUsers(subtask.getUsers());
                subDepotFile.setFiles(subtaskFile.getFiles());
                subDepotFile.setSubDepot(subDepot);
                // 四类审核人重置
                Set<Users> usersSet = new HashSet<>();
                subDepotFile.setProofSet(usersSet);
                subDepotFile.setAuditSet(usersSet);
                subDepotFile.setCountSet(usersSet);
                subDepotFile.setApproveSet(usersSet);
                subDepotFileList.add(subDepotFile);
            }
        }
        sublibraryFilesRepository.saveAll(subDepotFileList);

    }

    // 根据子库文件查询其历史文件各版本
    public List<SubdepotFileHis> getSublibraryFilesHisList(String sublibraryFileId){
        SubDepotFile subDepotFile = getSublibraryFileById(sublibraryFileId);
        return sublibraryFilesHistoryRepository.findByLeastSubDepotFileAndIfTemp(subDepotFile, false);
    }

    // 根据子库id查询子库下的文件
    public List<SubDepotFile> getSublibraryFilesBySublibraryAndIfApprove(String sublibraryId, boolean ifApprove) {
        if(!sublibraryService.hasSublibraryById(sublibraryId)){
            throw new ResultException(ResultCode.SUBLIBRARY_ID_NOT_FOUND_ERROR);
        }
        return sublibraryFilesRepository.findBySubDepotAndIfApprove(sublibraryService.getSublibraryById(sublibraryId), ifApprove);
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
    public SubDepotFile getSublibraryFileById(String sublibraryFileId) {
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
        Users users = userService.getUserById(userId);
        int userSecretClass = users.getSecretClass();     //获取用户密级
        if(!hasSublibraryFileById(sublibraryFileId)){
            throw new ResultException(ResultCode.SUBLIBRARY_FILE_ID_NOT_FOUND_ERROR);
        }
        int fileSecretClass = getSublibraryFileById(sublibraryFileId).getSecretClass();
        // 用户只能下载小于等于自己密级的文件
        if(userSecretClass < fileSecretClass){
            throw new ResultException(ResultCode.SUBLIBRARY_FILE_DOWNLOAD_DENIED_ERROR);
        }
        SubDepotFile subDepotFile = getSublibraryFileById(sublibraryFileId);
        File exportFile = new File(FileUtils.getTempDirectoryPath() + File.separator + subDepotFile.getName() + "." + subDepotFile.getFiles().getPostfix());
        FileUtils.copyFile(new File(subDepotFile.getFiles().getLocalPath()), exportFile);
        DownloadLog downloadLog = new DownloadLog();
        downloadLog.setFileName(subDepotFile.getName());
        downloadLog.setUsers(users);
        downloadLog.setFiles(subDepotFile.getFiles());
        downloadLogsRepository.save(downloadLog);
        return exportFile;
    }

    // 根据子任务文件id修改文件基本信息(类型、密级、代号)
    public SubDepotFile updateSublibraryFileId(String sublibraryFileId, SubDepotFile subDepotFileArgs) {
        if(!hasSublibraryFileById(sublibraryFileId)){
            throw new ResultException(ResultCode.SUBLIBRARY_FILE_ID_NOT_FOUND_ERROR);
        }
        SubDepotFile subDepotFile = getSublibraryFileById(sublibraryFileId);
        if(subDepotFileArgs == null){
            throw new ResultException(ResultCode.SUBLIBRARY_FILE_ARGS_NOT_FOUND_ERROR);
        }
        if(!StringUtils.isEmpty(subDepotFileArgs.getType())){
            subDepotFile.setType(subDepotFileArgs.getType());
        }
        if(!StringUtils.isEmpty(subDepotFileArgs.getSecretClass())){
            subDepotFile.setSecretClass(subDepotFileArgs.getSecretClass());
        }
        if(!StringUtils.isEmpty(subDepotFileArgs.getProductNo())){
            subDepotFile.setProductNo(subDepotFileArgs.getProductNo());
        }
        if(!StringUtils.isEmpty(subDepotFileArgs.getFileNo())){
            subDepotFile.setFileNo(subDepotFileArgs.getFileNo());
        }
        return sublibraryFilesRepository.save(subDepotFile);
    }

    // 根据子库文件id删除文件
    public SubDepotFile deleteSublibraryFileId(String sublibraryFileId) {
        SubDepotFile subDepotFile = getSublibraryFileById(sublibraryFileId);
        if(subDepotFile.getState() >= ApplicationConfig.SUBLIBRARY_FILE_PROOFREAD && subDepotFile.getState() <= ApplicationConfig.SUBLIBRARY_FILE_AUDIT_OVER){                     // 审核中及审核后无权进行删除
            throw new ResultException(ResultCode.DELETE_DENIED_ERROR);
        }
        List<SubDepotFileAudit> subDepotFileAuditList = sublibraryFilesAuditRepository.findBySubDepotFile(subDepotFile);
        sublibraryFilesAuditRepository.deleteInBatch(subDepotFileAuditList);
        List<SubdepotFileHis> subdepotFileHisList = sublibraryFilesHistoryRepository.findByLeastSubDepotFile(subDepotFile);
        sublibraryFilesHistoryRepository.deleteInBatch(subdepotFileHisList);
        sublibraryFilesRepository.delete(subDepotFile);
        return subDepotFile;
     }

    // 选择文件（一批文件）的审核模式及四类审核人
    public List<SubDepotFile> arrangeAudit(String[] sublibraryFileId, int auditMode, String[] proofreadUserIds, String[] auditUserIds, String[] countersignUserIds, String[] approveUserIds){
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
        List<SubDepotFile> subDepotFileList = new ArrayList<>();
        for(String id : sublibraryFileId){
            if(!hasSublibraryFileById(id)){
                throw new ResultException(ResultCode.SUBLIBRARY_FILE_ID_NOT_FOUND_ERROR);
            }
            SubDepotFile subDepotFile = getSublibraryFileById(id);
            subDepotFile.setProofSet(idsToSet(proofreadUserIds));
            subDepotFile.setAuditSet(idsToSet(auditUserIds));
            if(auditMode != ApplicationConfig.AUDIT_NO_COUNTERSIGN){
                subDepotFile.setCountSet(idsToSet(countersignUserIds));
            }
            subDepotFile.setApproveSet(idsToSet(approveUserIds));
            subDepotFile.setAuditMode(auditMode);
            if(subDepotFile.getState() == ApplicationConfig.SUBLIBRARY_FILE_APPLY_FOR_MODIFY_APPROVE){
                subDepotFile.setState(ApplicationConfig.SUBLIBRARY_FILE_APPLY_FOR_MODIFY_APPROVE_OVER);
            }else {
                subDepotFile.setState(ApplicationConfig.SUBLIBRARY_FILE_TO_BE_AUDIT);
            }
            subDepotFile.setIfReject(false);
            subDepotFile.setIfApprove(false);
            subDepotFileList.add(subDepotFile);
        }

        return sublibraryFilesRepository.saveAll(subDepotFileList);
    }

    // 根据用户id数组，将用户数组转为set集合
    private Set<Users> idsToSet(String[] ids){
        Set<Users> usersEntities = new HashSet<>();
        if(!ArrayUtils.isEmpty(ids)){
            for(String id : ids){
                usersEntities.add(userService.getUserById(id));
            }
        }
        return usersEntities;
    }

    // 根据用户id查询待校对、待审核、待会签、待批准
    public Map<String, List> findToBeAuditedFilesByUserId(String userId){
        Users users = userService.getUserById(userId);
        Map<String, List> sublibraryFilesToBeAudited = new HashMap<>();
        sublibraryFilesToBeAudited.put("proofreadFiles", sublibraryFilesRepository.findByProofSetContaining(users));
        sublibraryFilesToBeAudited.put("auditFiles", sublibraryFilesRepository.findByAuditSetContaining(users));
        sublibraryFilesToBeAudited.put("countersignFiles", sublibraryFilesRepository.findByCountSetContaining(users));
        sublibraryFilesToBeAudited.put("approveFiles", sublibraryFilesRepository.findByApproveSet(users));
        sublibraryFilesToBeAudited.put("alreadyAudit", sublibraryFilesAuditRepository.findByUsersAndStateAndIfOver(users, ApplicationConfig.SUBLIBRARY_FILE_COUNTERSIGN, false));
        return sublibraryFilesToBeAudited;
    }

    // 审核操作
    public SubDepotFile sublibraryFileAudit(String sublibraryFileId, String userId, SubDepotFile subDepotFileArgs, SubDepotFileAudit subDepotFileAuditArgs){
        SubDepotFile subDepotFile = getSublibraryFileById(sublibraryFileId);
        Users users = userService.getUserById(userId);             // 登录的用户
        int state = subDepotFile.getState();

        if(StringUtils.isEmpty(String.valueOf(subDepotFileArgs.getState()))){
            throw new ResultException(ResultCode.SUBLIBRARY_FILE_STATE_NOT_FOUND_ERROR);
        }

        if(subDepotFileArgs.getState() == ApplicationConfig.SUBLIBRARY_FILE_PROOFREAD){        // 校对中设置状态为校对
            subDepotFile.setState(ApplicationConfig.SUBLIBRARY_FILE_PROOFREAD);
        }
        if(subDepotFileArgs.getState() != subDepotFile.getState()){
            throw new ResultException(ResultCode.CURRENT_PROGRESS_NOT_ARRIVE_ERROR);
        }
        SubDepotFileAudit subDepotFileAudit = new SubDepotFileAudit();      // 审核详情

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
        if(sublibraryFilesAuditRepository.existsBySubDepotFileAndUsersAndStateAndIfOver(subDepotFile, users, subDepotFileArgs.getState(), false)){
            throw new ResultException(ResultCode.USER_ALREADY_COUNTERSIGN_ERROR);
        }

        if(subDepotFileAuditArgs.isIfPass()){
            if(subDepotFile.getUsers().getId().equals(userId)){    // 自己无权通过
                throw new ResultException(ResultCode.USER_PASS_DENIED_ERROR);
            }
            if(subDepotFileArgs.getState() == ApplicationConfig.SUBLIBRARY_FILE_AUDIT){               // 当前为审核
                if(subDepotFileArgs.getAuditMode() == ApplicationConfig.SUBLIBRARY_FILE_AUDIT_NO_COUNTERSIGN){  // 无会签
                    subDepotFile.setState(ApplicationConfig.SUBLIBRARY_FILE_APPROVE);
                }else{
                    subDepotFile.setState(ApplicationConfig.SUBLIBRARY_FILE_COUNTERSIGN);
                }
                subDepotFileAudit.setState(subDepotFileArgs.getState());
            }else if(subDepotFileArgs.getState() == ApplicationConfig.SUBLIBRARY_FILE_COUNTERSIGN && subDepotFileArgs.getAuditMode() == ApplicationConfig.SUBLIBRARY_FILE_AUDIT_MANY_COUNTERSIGN){
                // 当前为会签 且模式为多人会签
                if((subDepotFile.getManyCounterSignState() + 1) != subDepotFile.getCountSet().size()){
                    subDepotFile.setManyCounterSignState(subDepotFile.getManyCounterSignState() + 1);
                }else{                          // 所有人都已会签过
                    subDepotFile.setState(subDepotFileArgs.getState() + 1);
                }
                subDepotFileAudit.setState(subDepotFileArgs.getState());
            }else if(subDepotFileArgs.getState() == ApplicationConfig.SUBLIBRARY_FILE_APPROVE){           // 当前为批准
                subDepotFile.setIfApprove(true);                                                          // 子库文件通过审核
                subDepotFile.setState(ApplicationConfig.SUBLIBRARY_FILE_AUDIT_OVER);                          // 审批结束
                subDepotFileAudit.setState(subDepotFileArgs.getState());
            }else{
                subDepotFile.setState(subDepotFileArgs.getState() + 1);
                subDepotFileAudit.setState(subDepotFileArgs.getState());                         // 在哪步驳回
            }
        }else{                // 驳回
            if(state == ApplicationConfig.SUBLIBRARY_FILE_APPLY_FOR_MODIFY_APPROVE_OVER) {             // 二次修改被驳回后仍为二次修改状态
                subDepotFile.setState(ApplicationConfig.SUBLIBRARY_FILE_APPLY_FOR_MODIFY_APPROVE);
            }else {
                subDepotFile.setState(ApplicationConfig.SUBLIBRARY_FILE_AUDIT_OVER);                          // 审批结束
            }
            subDepotFileAudit.setState(subDepotFileArgs.getState());
            subDepotFile.setRejectState(subDepotFileArgs.getState());

            subDepotFile.setIfReject(true);           // 设置驳回状态为true
        }
        /**
         * 审核模式、审核阶段、审核结果、审核人、审核意见、当前步骤结束
         * */
        subDepotFileAudit.setIfPass(subDepotFileAuditArgs.isIfPass());               // 审核结果
        subDepotFileAudit.setUsers(users);                                          // 审核人
        subDepotFileAudit.setSubDepotFile(subDepotFile);                    // 审核详情所属子库文件
        subDepotFileAudit.setAuditDescription(subDepotFileAuditArgs.getAuditDescription());   // 审核意见
        sublibraryFilesAuditRepository.save(subDepotFileAudit);

        // 批准通过后 或 驳回后, 将详情改为已结束
        if(!(subDepotFileAuditArgs.isIfPass()) || subDepotFileArgs.getState() == ApplicationConfig.SUBLIBRARY_FILE_APPROVE){
            List<SubDepotFileAudit> subDepotFileAuditList = sublibraryFilesAuditRepository.findBySubDepotFile(subDepotFile);
            for(SubDepotFileAudit subDepotFileAudit1 : subDepotFileAuditList){
                subDepotFileAudit1.setIfOver(true);
            }
            sublibraryFilesAuditRepository.saveAll(subDepotFileAuditList);
        }
        return subDepotFile;
    }

    // 申请二次修改
    public SubDepotFile applyForModify(String sublibraryFileId, String userId){
        SubDepotFile subDepotFile = getSublibraryFileById(sublibraryFileId);
        Users user = userService.getUserById(userId);
        if(subDepotFile.getState() != ApplicationConfig.SUBLIBRARY_FILE_AUDIT_OVER){
            throw new ResultException(ResultCode.SECOND_MODIFY_DENIED_ERROR);
        }
        subDepotFile.setState(ApplicationConfig.SUBLIBRARY_FILE_APPLY_FOR_MODIFY);
        subDepotFile.setApplicant(user);
        return sublibraryFilesRepository.save(subDepotFile);
    }

    // 系统管理员查询所有待审核的二次修改申请
    public List<SubDepotFile> findByState(){
        return sublibraryFilesRepository.findByState(ApplicationConfig.SUBLIBRARY_FILE_APPLY_FOR_MODIFY);
    }

    // 系统管理员处理二次修改申请
    public SubDepotFile handleModifyApply(String sublibraryFileId, boolean ifModifyApprove){
        SubDepotFile subDepotFile = getSublibraryFileById(sublibraryFileId);
        subDepotFile.setIfModifyApprove(ifModifyApprove);
        subDepotFile.setIfApprove(false);
        if(ifModifyApprove){
            subDepotFile.setState(ApplicationConfig.SUBLIBRARY_FILE_APPLY_FOR_MODIFY_APPROVE);
            subDepotFile.setUsers(subDepotFile.getApplicant());
        }else{
            subDepotFile.setState(ApplicationConfig.SUBLIBRARY_FILE_AUDIT_OVER);
            subDepotFile.setApplicant(null);
        }
        return sublibraryFilesRepository.save(subDepotFile);
    }

    // 根据用户查询待其二次修改的文件  即根据二次修改是否通过状态以及申请人查询子库文件
    public List<SubDepotFile> findModifyFilesByApplicant(String userId, String subdepotId){
        Users applicant = userService.getUserById(userId);
        SubDepot subDepot = sublibraryService.getSublibraryById(subdepotId);
        return sublibraryFilesRepository.findByIfModifyApproveAndStateAndApplicantAndSubDepot(true, ApplicationConfig.SUBLIBRARY_FILE_APPLY_FOR_MODIFY_APPROVE, applicant, subDepot);
    }

    // 驳回后  修改  [id 是否是直接修改 驳回修改内容是否提交到第一个流程（直接修改需要） 文件 版本（二次修改需要）]
    public SubDepotFile modifySublibraryFile(String sublibraryFileId, FileMeta fileMeta){
        SubDepotFile subDepotFile = getSublibraryFileById(sublibraryFileId);

        if(fileMeta.isIfDirectModify() && !(subDepotFile.getState() == ApplicationConfig.SUBLIBRARY_FILE_AUDIT_OVER && subDepotFile.isIfReject())){              // 只有审核结束并且被驳回才能直接修改
            throw new ResultException(ResultCode.MODIFY_DENIED_ERROR);
        }
        if(!fileMeta.isIfDirectModify() && (subDepotFile.getState() != ApplicationConfig.SUBLIBRARY_FILE_APPLY_FOR_MODIFY_APPROVE)){              // 只有通过二次修改申请才能进行二次修改
            throw new ResultException(ResultCode.MODIFY_DENIED_ERROR);
        }

        if(StringUtils.isEmpty(fileMeta.isIfDirectModify())){
            throw new ResultException(ResultCode.FILE_MODIFYWAY_NOT_FOUND_ERROR);
        }

        if(fileMeta.getSecretClass() > subDepotFile.getUsers().getSecretClass()){
            throw new ResultException(ResultCode.SUBLIBRARY_FILE_UPLOAD_DENIED);
        }

        // 修改前存储此文件的备份 若备份已存在删除上一备份  直接修改、二次修改都保留可以撤销的备份
        if(sublibraryFilesHistoryRepository.existsByLeastSubDepotFileAndIfTemp(subDepotFile, true)){
            sublibraryFilesHistoryRepository.delete(sublibraryFilesHistoryRepository.findByLeastSubDepotFileAndIfTemp(subDepotFile, true).get(0));
        }
        saveSublibraryFilesHistoryBySublibraryFile(subDepotFile, true);

        if(fileMeta.isIfDirectModify()){            // 直接修改
            if(fileMeta.isIfBackToStart()){                // 驳回后的修改提交到第一个流程
                subDepotFile.setAuditSet(null);
                subDepotFile.setState(ApplicationConfig.SUBLIBRARY_FILE_TO_BE_AUDIT);
            }else{
                subDepotFile.setState(subDepotFile.getRejectState());
            }

        }else{                 // 二次修改
            // 判断文件是否通过二次修改申请
            if(!subDepotFile.isIfModifyApprove()){
                throw new ResultException(ResultCode.MODIFY_APPROVE_NOT_PASS_ERROR);
            }
            // 二次修改
            if(StringUtils.isEmpty(fileMeta.getVersion())){
                throw new ResultException(ResultCode.FILE_VERSION_NOT_FOUND_ERROR);
            }
            // 修改前保存此文件历史
            saveSublibraryFilesHistoryBySublibraryFile(subDepotFile, false);

            subDepotFile.setVersion(fileMeta.getVersion());
            // subDepotFile.setState(0);
            subDepotFile.setAuditMode(0);
            subDepotFile.setIfModifyApprove(false);
            subDepotFile.setIfApprove(false);

            // 四类审核人重置
            Set<Users> usersSet = new HashSet<>();
            subDepotFile.setProofSet(usersSet);
            subDepotFile.setAuditSet(usersSet);
            subDepotFile.setCountSet(usersSet);
            subDepotFile.setApproveSet(usersSet);
        }

        if(!StringUtils.isEmpty(fileMeta)){            // 库文件驳回后修改，可以只修改基本信息，即可以不修改文件内容
            subDepotFile.setName(StringUtils.isEmpty(FilenameUtils.getBaseName(fileMeta.getRelativePath())) ? "-" : FilenameUtils.getBaseName(fileMeta.getRelativePath()));
            subDepotFile.setCreateTime(new Date());
            subDepotFile.setPostfix(FilenameUtils.getExtension(fileMeta.getRelativePath()));
            subDepotFile.setType(fileMeta.getType());
            subDepotFile.setSecretClass(fileMeta.getSecretClass());
            subDepotFile.setProductNo(fileMeta.getProductNo());
            subDepotFile.setFileNo(fileMeta.getFileNo());
            subDepotFile.setIfApprove(false);
            subDepotFile.setIfReject(false);
            subDepotFile.setRejectState(0);
            subDepotFile.setManyCounterSignState(0);           // 多人会签模式，此时无人开始会签
            subDepotFile.setFiles(fileService.getFileById(fileMeta.getFileId()));
        }
        return sublibraryFilesRepository.save(subDepotFile);
    }

    // 从子库文件生成子库文件历史
    public void saveSublibraryFilesHistoryBySublibraryFile(SubDepotFile sourceNode, boolean ifTemp) {
        SubdepotFileHis copyNode = new SubdepotFileHis();
        BeanUtils.copyProperties(sourceNode, copyNode, "id", "create_time", "leastSubDepotFile");
        copyNode.setLeastSubDepotFile(sourceNode);
        copyNode.setIfTemp(ifTemp);
        sublibraryFilesHistoryRepository.save(copyNode);
    }

    // 从子库文件历史生成子库文件
    public void saveSublibraryFilesBySublibraryFile(SubDepotFile coverNode, SubdepotFileHis sourceNode) {
        BeanUtils.copyProperties(sourceNode, coverNode, "id", "create_time", "state", "rejectState", "leastSubDepotFile", "ifDirectModify", "approveUsersSet", "auditUserSet", "countersignUserSet", "approveUserSet");
        /*Set<Users> approveUserSet = sourceNode.getApproveUsersSet();
        coverNode.setApproveUsersSet(null);
        coverNode.setApproveUsersSet(approveUserSet);*/
        coverNode.setState(sourceNode.getState());
        coverNode.setRejectState(sourceNode.getRejectState());
        sublibraryFilesRepository.save(coverNode);
    }

    // 根据子库文件id查询是否有可撤销文件(临时文件)
    public boolean ifHasTemp(String sublibraryFileId){
        return sublibraryFilesHistoryRepository.existsByLeastSubDepotFileAndIfTemp(getSublibraryFileById(sublibraryFileId), true);
    }

    // 根据子库文件id查找其历史版本文件
    public List<SubdepotFileHis> getSublibraryHistoriesFiles(String sublibraryFileId){
        return sublibraryFilesHistoryRepository.findByLeastSubDepotFileAndIfTemp(getSublibraryFileById(sublibraryFileId), false);
    }

    // 撤销文件操作    只支持一次撤销操作，撤销回上一步  要再次撤销需再次修改
    public SubDepotFile revokeModify(String sublibraryFileId){
        if(!ifHasTemp(sublibraryFileId)){       // 当前文件不存在可撤销的文件
            throw new ResultException(ResultCode.FILE_HAS_NO_REVOKE_FILE);
        }
        // 当前文件
        SubDepotFile subDepotFile = getSublibraryFileById(sublibraryFileId);
        // 需撤销至的文件(该文件的临时文件)
        SubdepotFileHis sourceNode = sublibraryFilesHistoryRepository.findByLeastSubDepotFileAndIfTemp(subDepotFile, true).get(0);

        // 历史版本提为当前文件
        saveSublibraryFilesBySublibraryFile(subDepotFile, sourceNode);
        sublibraryFilesHistoryRepository.delete(sourceNode);
        // 当前文件存为历史
        // saveSublibraryFilesHistoryBySublibraryFile(subDepotFile, true);

        return getSublibraryFileById(sublibraryFileId);
    }


    // 更换版本（二次修改可恢复其中任意版本）   版本为空则为直接修改，否则为二次修改，切换到指定版本
    public SubDepotFile versionReplace(String sublibraryFileId, String version){
        // 当前文件
        SubDepotFile subDepotFile = getSublibraryFileById(sublibraryFileId);
        // 需撤销至的文件
        SubdepotFileHis sourceNode = sublibraryFilesHistoryRepository.findByLeastSubDepotFileAndIfTempAndVersion(subDepotFile, false, version);

        // 历史版本提为当前文件
        saveSublibraryFilesBySublibraryFile(subDepotFile, sourceNode);
        sublibraryFilesHistoryRepository.delete(sourceNode);

        return getSublibraryFileById(sublibraryFileId);
    }

    // 返回整个系统的所有库及其下子库及子库下的文件
    public List<Map<String, Object>> getLibraryTrees(){
        List<Map<String, Object>> list = new ArrayList<>();
        List<Depot> depotList = libraryService.getAll();
        for(Depot depot : depotList){
            List<Map<String, Object>> sublibraryList = new ArrayList<Map<String, Object>>();
            List<SubDepot> subDepotList = sublibraryService.getSublibrariesByLibraryId(depot.getId());
            for(SubDepot subDepot : subDepotList){
                Map<String, Object> map = new HashMap<>();
                map.put("sublibraryLibrary", subDepot);
                map.put("sublibraryLibraryFiles", getSublibraryFilesBySublibraryAndIfApprove(subDepot.getId(), true));
                sublibraryList.add(map);
            }
            Map<String, Object> map = new HashMap<>();
            map.put("library", depot);
            map.put("sublibraryLibraries", sublibraryList);
            list.add(map);
        }
        return list;
    }

    // 根据用户查询自己未通过的子库文件
    public List<SubDepotFile> getFailedFilesByUser(String userId, String subDepotId){
        Users users = userService.getUserById(userId);
        SubDepot subDepot = sublibraryService.getSublibraryById(subDepotId);
        return sublibraryFilesRepository.findBySubDepotAndUsersAndIfApprove(subDepot, users, false);
    }
}
