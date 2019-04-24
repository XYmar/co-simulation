package com.rengu.cosimulation.controller;

import com.rengu.cosimulation.entity.FileMetaEntity;
import com.rengu.cosimulation.entity.ResultEntity;
import com.rengu.cosimulation.entity.SublibraryFilesAuditEntity;
import com.rengu.cosimulation.entity.SublibraryFilesEntity;
import com.rengu.cosimulation.repository.SublibraryFilesRepository;
import com.rengu.cosimulation.service.SublibraryFilesService;
import com.rengu.cosimulation.specification.Filter;
import com.rengu.cosimulation.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.rengu.cosimulation.specification.SpecificationBuilder.selectFrom;

/**
 * Author: XYmar
 * Date: 2019/4/1 12:53
 */
@Slf4j
@RestController
@RequestMapping(value = "/sublibraryFiles")
public class SublibraryFilesController {
    private final SublibraryFilesService sublibraryFilesService;
    private final SublibraryFilesRepository sublibraryFilesRepository;

    @Autowired
    public SublibraryFilesController(SublibraryFilesService sublibraryFilesService, SublibraryFilesRepository sublibraryFilesRepository) {
        this.sublibraryFilesService = sublibraryFilesService;
        this.sublibraryFilesRepository = sublibraryFilesRepository;
    }

    // 根据Id导出子库文件
    @GetMapping(value = "/{sublibraryFileId}/user/{userId}/export")
    public void exportSublibraryFileById(@PathVariable(value = "sublibraryFileId") String sublibraryFileId, @PathVariable(value = "userId") String userId, HttpServletResponse httpServletResponse) throws IOException {
        File exportFile = sublibraryFilesService.exportSublibraryFileById(sublibraryFileId, userId);
        String mimeType = URLConnection.guessContentTypeFromName(exportFile.getName()) == null ? "application/octet-stream" : URLConnection.guessContentTypeFromName(exportFile.getName());
        httpServletResponse.setContentType(mimeType);
        httpServletResponse.setHeader("Content-Disposition", "attachment;filename=" + new String(exportFile.getName().getBytes(StandardCharsets.UTF_8), "ISO8859-1"));
        httpServletResponse.setContentLengthLong(exportFile.length());
        // 文件流输出
        IOUtils.copy(new FileInputStream(exportFile), httpServletResponse.getOutputStream());
        httpServletResponse.flushBuffer();
    }

    // 根据子库文件id查询文件信息
    @GetMapping(value = "/{sublibraryFileId}")
    public ResultEntity getSublibraryFileId(@PathVariable(value = "sublibraryFileId") String sublibraryFileId){
        return ResultUtils.success(sublibraryFilesService.getSublibraryFileById(sublibraryFileId));
    }

    // 根据子库文件id修改文件基本信息
    @PatchMapping(value = "/{sublibraryFileId}")
    public ResultEntity updateSublibraryFileId(@PathVariable(value = "sublibraryFileId") String sublibraryFileId, SublibraryFilesEntity sublibraryFilesEntity){
        return ResultUtils.success(sublibraryFilesService.updateSublibraryFileId(sublibraryFileId, sublibraryFilesEntity));
    }

    // 根据子库文件id删除文件
    @DeleteMapping(value = "/{sublibraryFileId}")
    public ResultEntity deleteSublibraryFileId(@PathVariable(value = "sublibraryFileId") String sublibraryFileId) {
        return ResultUtils.success(sublibraryFilesService.deleteSublibraryFileId(sublibraryFileId));
    }

    // 根据子库id查询所有文件
    @GetMapping(value = "bySublibraryId/{sublibraryId}/files")
    public ResultEntity getSublibraryFilesByLibraryId(@PathVariable(value = "sublibraryId") String sublibraryId, boolean ifApprove){
        return ResultUtils.success(sublibraryFilesService.getSublibraryFilesBySublibraryAndIfApprove(sublibraryId, ifApprove));
    }

    // 选择子库提交文件（一批）审核人
    @PatchMapping(value = "/arrangeAudit")
    public ResultEntity arrangeAudit(String[] sublibraryFileId, int auditMode, String[] proofreadUserIds, String[] auditUserIds, String[] countersignUserIds, String[] approveUserIds){
        return ResultUtils.success(sublibraryFilesService.arrangeAudit(sublibraryFileId, auditMode, proofreadUserIds, auditUserIds, countersignUserIds,approveUserIds));
    }

    // 根据用户id查询待校对、待审核、待会签、待批准
    @GetMapping(value = "/{userId}/findToBeAuditedFiles")
    public ResultEntity findToBeAuditedFilesByUserId(@PathVariable(value = "userId") String userId){
        return ResultUtils.success(sublibraryFilesService.findToBeAuditedFilesByUserId(userId));
    }

    // 审核操作
    @PatchMapping(value = "/{sublibraryFileId}/sublibraryFileAudit")
    public ResultEntity sublibraryFileAudit(@PathVariable(value = "sublibraryFileId") String sublibraryFileId, String userId, SublibraryFilesEntity sublibraryFilesEntityArgs, SublibraryFilesAuditEntity sublibraryFilesAuditEntityArgs){
        return ResultUtils.success(sublibraryFilesService.sublibraryFileAudit(sublibraryFileId, userId,sublibraryFilesEntityArgs, sublibraryFilesAuditEntityArgs));
    }

    // 申请二次修改
    @PostMapping(value = "/{sublibraryFileId}/applyForModify")
    public ResultEntity applyForModify(@PathVariable(value = "sublibraryFileId") String sublibraryFileId){
        return ResultUtils.success(sublibraryFilesService.applyForModify(sublibraryFileId));
    }

    // 系统管理员查询所有待审核的二次修改申请
    @GetMapping(value = "/findModifyToBeAudit")
    @PreAuthorize(value = "hasRole('ADMIN')")
    public ResultEntity findModifyToBeAudit() {
        return ResultUtils.success(sublibraryFilesService.findByState());
    }


    // 系统管理员处理二次修改申请
    @PostMapping(value = "/{sublibraryFileId}/handleModifyApply")
    @PreAuthorize(value = "hasRole('ADMIN')")
    public ResultEntity handleModifyApply(@PathVariable(value = "sublibraryFileId") String sublibraryFileId, boolean ifModifyApprove){
        return ResultUtils.success(sublibraryFilesService.handleModifyApply(sublibraryFileId, ifModifyApprove));
    }

    // 驳回后的提交
    @PostMapping(value = "/{sublibraryFileId}/modifySublibraryFile")
    public ResultEntity modifySublibraryFile(@PathVariable(value = "sublibraryFileId") String sublibraryFileId, @RequestBody FileMetaEntity fileMetaEntity){
        return ResultUtils.success(sublibraryFilesService.modifySublibraryFile(sublibraryFileId, fileMetaEntity));
    }

    // 根据子库文件id查找其历史版本文件
    @GetMapping(value = "/{sublibraryFileId}/getSublibraryHistoriesFiles")
    public ResultEntity getSublibraryHistoriesFiles(@PathVariable(value = "sublibraryFileId") String sublibraryFileId) {
        return ResultUtils.success(sublibraryFilesService.getSublibraryHistoriesFiles(sublibraryFileId));
    }

    // 撤销修改
    @PatchMapping(value = "/{sublibraryFileId}/revokeModify")
    public ResultEntity revokeModify(@PathVariable(value = "sublibraryFileId") String sublibraryFileId){
        return ResultUtils.success(sublibraryFilesService.revokeModify(sublibraryFileId));
    }

    // 更换版本
    @PatchMapping(value = "/{sublibraryFileId}/versionReplace")
    public ResultEntity versionReplace(@PathVariable(value = "sublibraryFileId") String sublibraryFileId, String version){
        return ResultUtils.success(sublibraryFilesService.versionReplace(sublibraryFileId, version));
    }

    // 项目关键字查询
    @PostMapping("/multiInquire")
    public ResultEntity filter(@RequestBody Filter filter){
        return ResultUtils.success(selectFrom(sublibraryFilesRepository).where(filter).findAll());
    }
}
