package com.rengu.cosimulation.controller;

import com.rengu.cosimulation.entity.ProDesignLinkFilesEntity;
import com.rengu.cosimulation.entity.ResultEntity;
import com.rengu.cosimulation.service.ProDesignLinkFilesService;
import com.rengu.cosimulation.utils.ResultUtils;
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
 * Date: 2019/3/5 11:03
 */
@RestController
@RequestMapping(value = "/proDesignLinkFiles")
public class ProDesignLinkFilesController {
    private final ProDesignLinkFilesService proDesignLinkFilesService;

    @Autowired
    public ProDesignLinkFilesController(ProDesignLinkFilesService proDesignLinkFilesService) {
        this.proDesignLinkFilesService = proDesignLinkFilesService;
    }

    // 根据Id导出组件文件
    @GetMapping(value = "/{proDesignLinkFileId}/user/{userId}/export")
    public void exportProDesignLinkFileById(@PathVariable(value = "proDesignLinkFileId") String proDesignLinkFileId, @PathVariable(value = "userId") String userId, HttpServletResponse httpServletResponse) throws IOException {
        File exportFile = proDesignLinkFilesService.exportProDesignLinkFileById(proDesignLinkFileId, userId);
        String mimeType = URLConnection.guessContentTypeFromName(exportFile.getName()) == null ? "application/octet-stream" : URLConnection.guessContentTypeFromName(exportFile.getName());
        httpServletResponse.setContentType(mimeType);
        httpServletResponse.setHeader("Content-Disposition", "attachment;filename=" + new String(exportFile.getName().getBytes(StandardCharsets.UTF_8), "ISO8859-1"));
        httpServletResponse.setContentLengthLong(exportFile.length());
        // 文件流输出
        IOUtils.copy(new FileInputStream(exportFile), httpServletResponse.getOutputStream());
        httpServletResponse.flushBuffer();
    }

    // 根据子任务文件id查询文件信息
    @GetMapping(value = "/{proDesignLinkFileId}")
    public ResultEntity getProDesignLinkFileId(@PathVariable(value = "proDesignLinkFileId") String proDesignLinkFileId){
        return ResultUtils.success(proDesignLinkFilesService.getProDesignLinkFileById(proDesignLinkFileId));
    }

    // 根据子任务文件id修改文件基本信息
    @PatchMapping(value = "/{proDesignLinkFileId}")
    public ResultEntity updateProDesignLinkFileId(@PathVariable(value = "proDesignLinkFileId") String proDesignLinkFileId, ProDesignLinkFilesEntity proDesignLinkFilesEntity){
        return ResultUtils.success(proDesignLinkFilesService.updateProDesignLinkFileId(proDesignLinkFileId, proDesignLinkFilesEntity));
    }
}
