package com.rengu.cosimulation.controller;

import com.rengu.cosimulation.entity.ChunkEntity;
import com.rengu.cosimulation.entity.FileEntity;
import com.rengu.cosimulation.entity.ResultEntity;
import com.rengu.cosimulation.service.FileService;
import com.rengu.cosimulation.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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

    // 根据MD5检查文件是否存在
    @GetMapping(value = "/hasmd5")
    public ResultEntity hasFileByMD5(@RequestParam(value = "MD5") String MD5) {
        return ResultUtils.success(fileService.hasFileByMD5(MD5) ? fileService.getFileByMD5(MD5) : fileService.hasFileByMD5(MD5));
    }

    // 检查文件块是否存在
    @PostMapping(value = "/chunks")
    public void saveChunk(ChunkEntity chunkEntity, @RequestParam(value = "file") MultipartFile multipartFile) throws IOException {
        fileService.saveChunk(chunkEntity, multipartFile);
    }

    // 合并文件块
    @PostMapping(value = "/chunks/merge")
    public ResultEntity mergeChunks(ChunkEntity chunkEntity, FileEntity fileEntity) throws IOException {
        return ResultUtils.success(fileService.mergeChunks(chunkEntity, fileEntity));
    }
}
