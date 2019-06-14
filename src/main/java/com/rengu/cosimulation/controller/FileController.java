package com.rengu.cosimulation.controller;

import com.rengu.cosimulation.entity.Chunk;
import com.rengu.cosimulation.entity.Result;
import com.rengu.cosimulation.service.FileService;
import com.rengu.cosimulation.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Author: XYmar
 * Date: 2019/2/28 14:58
 */
@RestController
@RequestMapping(value = "/files")
@Slf4j
public class FileController {
    private final FileService fileService;

    @Autowired
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    // 检查文件块是否存在
    @GetMapping(value = "/chunks")
    public void hasChunk(HttpServletResponse httpServletResponse, Chunk chunk) {
        if (!fileService.hasChunk(chunk)) {
            httpServletResponse.setStatus(HttpServletResponse.SC_GONE);
        }
    }

    // 根据MD5检查文件是否存在
    @GetMapping(value = "/hasmd5")
    public Result hasFileByMD5(@RequestParam(value = "MD5") String MD5) {
        return ResultUtils.success(fileService.hasFileByMD5(MD5) ? fileService.getFileByMD5(MD5) : fileService.hasFileByMD5(MD5));
    }

    // 检查文件块是否存在
    @PostMapping(value = "/chunks")
    public void saveChunk(Chunk chunk, @RequestParam(value = "file") MultipartFile multipartFile) throws IOException {
        fileService.saveChunk(chunk, multipartFile);
    }

    // 合并文件块
    @PostMapping(value = "/chunks/merge")
    public Result mergeChunks(Chunk chunk) throws IOException, ExecutionException, InterruptedException {
        return ResultUtils.success(fileService.mergeChunks(chunk));
    }
}
