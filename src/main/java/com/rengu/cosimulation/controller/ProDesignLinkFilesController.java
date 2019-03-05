package com.rengu.cosimulation.controller;

import com.rengu.cosimulation.service.ProDesignLinkFilesService;
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
}
