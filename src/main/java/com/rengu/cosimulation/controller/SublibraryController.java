package com.rengu.cosimulation.controller;

import com.rengu.cosimulation.entity.FileMetaEntity;
import com.rengu.cosimulation.entity.ResultEntity;
import com.rengu.cosimulation.entity.SublibraryEntity;
import com.rengu.cosimulation.service.SublibraryFilesService;
import com.rengu.cosimulation.service.SublibraryService;
import com.rengu.cosimulation.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Author: XYmar
 * Date: 2019/3/29 17:46
 */
@Slf4j
@RestController
@RequestMapping(value = "/sublibraries")
public class SublibraryController {
    private final SublibraryService subLibraryService;
    @Autowired
    private SublibraryFilesService sublibraryFilesService;

    @Autowired
    public SublibraryController(SublibraryService subLibraryService) {
        this.subLibraryService = subLibraryService;
    }

    // 根据库id保存子库
    @PostMapping
    public ResultEntity saveSublibrary(SublibraryEntity sublibraryEntity, String libraryId){
        return ResultUtils.success(subLibraryService.saveSublibrary(sublibraryEntity,libraryId));
    }

    // 根据id修改子库
    @PatchMapping(value = "/{sublibraryId}")
    public ResultEntity updateSublibraryById(@PathVariable(value = "sublibraryId") String sublibraryId, SublibraryEntity sublibraryEntityArgs){
        return ResultUtils.success(subLibraryService.updateSublibraryById(sublibraryId, sublibraryEntityArgs));
    }

    // 根据id删除子库
    @DeleteMapping(value = "/{sublibraryId}")
    public ResultEntity deleteSublibraryById(@PathVariable(value = "sublibraryId") String sublibraryId){
        return ResultUtils.success(subLibraryService.deleteSublibraryById(sublibraryId));
    }

    // 根据库id查询其所有子库
    @GetMapping(value = "/byLibraryId/{libraryId}")
    public ResultEntity getSublibrariesByLibraryId(@PathVariable(value = "libraryId") String libraryId){
        return ResultUtils.success(subLibraryService.getSublibrariesByLibraryId(libraryId));
    }

    // 根据子库id创建文件
    @PostMapping(value = "/{sublibraryId}/uploadfiles")
    public ResultEntity saveSublibraryFilesBySublibraryId(@PathVariable(value = "sublibraryId") String sublibraryId,  @RequestBody List<FileMetaEntity> fileMetaEntityList){
        return ResultUtils.success(sublibraryFilesService.saveSublibraryFilesBySublibraryId(sublibraryId, fileMetaEntityList));
    }
}
