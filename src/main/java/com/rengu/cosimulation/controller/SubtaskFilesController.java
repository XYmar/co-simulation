package com.rengu.cosimulation.controller;

import com.rengu.cosimulation.entity.SubtaskFilesEntity;
import com.rengu.cosimulation.entity.ResultEntity;
import com.rengu.cosimulation.service.SubtaskFilesService;
import com.rengu.cosimulation.utils.ResultUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

/**
 * Author: XYmar
 * Date: 2019/3/5 11:03
 */
@RestController
@RequestMapping(value = "/subtaskFiles")
public class SubtaskFilesController {
    private final SubtaskFilesService subtaskFilesService;

    @Autowired
    public SubtaskFilesController(SubtaskFilesService subtaskFilesService) {
        this.subtaskFilesService = subtaskFilesService;
    }

    // 根据Id导出子任务文件
    @GetMapping(value = "subtaskFile/{subtaskFileId}/user/{userId}/export")
    public void exportSubtaskFileById(@PathVariable(value = "subtaskFileId") String subtaskFileId, @PathVariable(value = "userId") String userId, HttpServletResponse httpServletResponse) throws IOException {
        File exportFile = subtaskFilesService.exportSubtaskFileById(subtaskFileId, userId);
        String mimeType = URLConnection.guessContentTypeFromName(exportFile.getName()) == null ? "application/octet-stream" : URLConnection.guessContentTypeFromName(exportFile.getName());
        httpServletResponse.setContentType(mimeType);
        httpServletResponse.setHeader("Content-Disposition", "attachment;filename=" + new String(exportFile.getName().getBytes(StandardCharsets.UTF_8), "ISO8859-1"));
        httpServletResponse.setContentLengthLong(exportFile.length());
        // 文件流输出
        IOUtils.copy(new FileInputStream(exportFile), httpServletResponse.getOutputStream());
        httpServletResponse.flushBuffer();
    }

    // 根据子任务文件id查询文件信息
    @GetMapping(value = "/{subtaskFileId}")
    public ResultEntity getSubtaskFileId(@PathVariable(value = "subtaskFileId") String subtaskFileId){
        return ResultUtils.success(subtaskFilesService.getSubtaskFileById(subtaskFileId));
    }

    // 根据子任务文件id修改文件基本信息
    @PatchMapping(value = "/{subtaskFileId}")
    public ResultEntity updateSubtaskFileId(@PathVariable(value = "subtaskFileId") String subtaskFileId, SubtaskFilesEntity subtaskFilesEntity){
        return ResultUtils.success(subtaskFilesService.updateSubtaskFileId(subtaskFileId, subtaskFilesEntity));
    }

    // 根据子任务文件id删除文件
    // TODO 只有项目管理员登录成功后，才能删除吗
    @DeleteMapping(value = "/{subtaskFileId}")
    //@PreAuthorize(value = "hasRole('PROJECT_MANAGER')")
    public ResultEntity deleteSubtaskFileId(@PathVariable(value = "subtaskFileId") String subtaskFileId){
        return ResultUtils.success(subtaskFilesService.deleteSubtaskFileId(subtaskFileId));
    }
}
