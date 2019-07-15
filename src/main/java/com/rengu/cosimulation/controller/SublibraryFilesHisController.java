package com.rengu.cosimulation.controller;

import com.rengu.cosimulation.service.SublibraryFilesHisService;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

/**
 * Author: XYmar
 * Date: 2019/7/2 16:11
 */
@RestController
@RequestMapping("/sublibraryFilesHis")
public class SublibraryFilesHisController {
    private final SublibraryFilesHisService sublibraryFilesHisService;

    @Autowired
    public SublibraryFilesHisController(SublibraryFilesHisService sublibraryFilesHisService) {
        this.sublibraryFilesHisService = sublibraryFilesHisService;
    }

    // 根据Id导出子库历史文件
    @GetMapping(value = "/{sublibraryFileHisId}/user/{userId}/export")
    public void exportSublibraryFileHisById(@PathVariable(value = "sublibraryFileHisId") String sublibraryFileHisId, @PathVariable(value = "userId") String userId, HttpServletResponse httpServletResponse) throws IOException {
        File exportFileHis = sublibraryFilesHisService.exportSublibraryFileHisById(sublibraryFileHisId, userId);
        String mimeType = URLConnection.guessContentTypeFromName(exportFileHis.getName()) == null ? "application/octet-stream" : URLConnection.guessContentTypeFromName(exportFileHis.getName());
        httpServletResponse.setContentType(mimeType);
        httpServletResponse.setHeader("Content-Disposition", "attachment;filename=" + new String(exportFileHis.getName().getBytes(StandardCharsets.UTF_8), "ISO8859-1"));
        httpServletResponse.setContentLengthLong(exportFileHis.length());
        // 文件流输出
        IOUtils.copy(new FileInputStream(exportFileHis), httpServletResponse.getOutputStream());
        httpServletResponse.flushBuffer();
    }
}
