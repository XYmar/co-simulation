package com.rengu.cosimulation.controller;

import com.rengu.cosimulation.entity.FileMeta;
import com.rengu.cosimulation.entity.Result;
import com.rengu.cosimulation.entity.SubDepot;
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
    public Result saveSublibrary(SubDepot subDepot, String libraryId){
        return ResultUtils.success(subLibraryService.saveSublibrary(subDepot,libraryId));
    }

    // 根据id修改子库
    @PatchMapping(value = "/{sublibraryId}")
    public Result updateSublibraryById(@PathVariable(value = "sublibraryId") String sublibraryId, SubDepot subDepotArgs){
        return ResultUtils.success(subLibraryService.updateSublibraryById(sublibraryId, subDepotArgs));
    }

    // 根据id删除子库
    @DeleteMapping(value = "/{sublibraryId}")
    public Result deleteSublibraryById(@PathVariable(value = "sublibraryId") String sublibraryId){
        return ResultUtils.success(subLibraryService.deleteSublibraryById(sublibraryId));
    }

    // 根据库id查询其所有子库
    @GetMapping(value = "/byLibraryId/{libraryId}")
    public Result getSublibrariesByLibraryId(@PathVariable(value = "libraryId") String libraryId){
        return ResultUtils.success(subLibraryService.getSublibrariesByLibraryId(libraryId));
    }

    // 根据子库id创建文件
    @PostMapping(value = "/{sublibraryId}/uploadfiles/byUser/{userId}")
    public Result saveSublibraryFilesBySublibraryId(@PathVariable(value = "sublibraryId") String sublibraryId, @PathVariable(value = "userId") String userId, @RequestBody List<FileMeta> fileMetaList){
        return ResultUtils.success(sublibraryFilesService.saveSublibraryFilesBySublibraryId(sublibraryId, userId, fileMetaList));
    }

    // 根据子库id查看是否有重复文件
    @PostMapping(value = "/{sublibraryId}/findExistSubDepotFiles/byUser/{userId}")
    public Result findExistSubDepotFiles(@PathVariable(value = "sublibraryId") String sublibraryId, @PathVariable(value = "userId") String userId, @RequestBody List<FileMeta> fileMetaList){
        return ResultUtils.success(sublibraryFilesService.findExistSubDepotFiles(sublibraryId, userId, fileMetaList));
    }
}
