package com.rengu.cosimulation.controller;

import com.rengu.cosimulation.entity.ResultEntity;
import com.rengu.cosimulation.entity.SublibraryFilesAuditEntity;
import com.rengu.cosimulation.entity.SublibraryFilesEntity;
import com.rengu.cosimulation.service.SublibraryFilesService;
import com.rengu.cosimulation.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

/**
 * Author: XYmar
 * Date: 2019/4/1 12:53
 */
@Slf4j
@RestController
@RequestMapping(value = "/sublibraryFiles")
public class SublibraryFilesController {
    private final SublibraryFilesService sublibraryFilesService;

    @Autowired
    public SublibraryFilesController(SublibraryFilesService sublibraryFilesService) {
        this.sublibraryFilesService = sublibraryFilesService;
    }

    // 根据Id导出子库文件
    @GetMapping(value = "sublibraryFile/{sublibraryFileId}/user/{userId}/export")
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
    public ResultEntity deleteSublibraryFileId(@PathVariable(value = "sublibraryFileId") String sublibraryFileId){
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
    @GetMapping(value = "/{userId}")
    public ResultEntity findToBeAuditedFilesByUserId(@PathVariable(value = "userId") String userId){
        return ResultUtils.success(sublibraryFilesService.findToBeAuditedFilesByUserId(userId));
    }

    // 审核操作
    @PatchMapping(value = "/{sublibraryFileId}/sublibraryFileAudit")
    public ResultEntity sublibraryFileAudit(@PathVariable(value = "sublibraryFileId") String sublibraryFileId, String userId, SublibraryFilesEntity sublibraryFilesEntityArgs, SublibraryFilesAuditEntity sublibraryFilesAuditEntityArgs){
        return ResultUtils.success(sublibraryFilesService.sublibraryFileAudit(sublibraryFileId, userId,sublibraryFilesEntityArgs, sublibraryFilesAuditEntityArgs));
    }
}
