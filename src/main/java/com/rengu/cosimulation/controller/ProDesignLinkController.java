package com.rengu.cosimulation.controller;

import com.rengu.cosimulation.entity.FileMetaEntity;
import com.rengu.cosimulation.entity.ResultEntity;
import com.rengu.cosimulation.service.ProDesignLinkFilesService;
import com.rengu.cosimulation.service.ProDesignLinkService;
import com.rengu.cosimulation.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Author: XYmar
 * Date: 2019/2/26 10:26
 */
@RestController
@RequestMapping(value = "/proDesignLink")
public class ProDesignLinkController {
    private final ProDesignLinkService proDesignLinkService;
    private final ProDesignLinkFilesService proDesignLinkFilesService;

    @Autowired
    public ProDesignLinkController(ProDesignLinkService proDesignLinkService, ProDesignLinkFilesService proDesignLinkFilesService) {
        this.proDesignLinkService = proDesignLinkService;
        this.proDesignLinkFilesService = proDesignLinkFilesService;
    }

    // 根据项目id查询子任务
    @GetMapping(value = "/byProject/{projectId}")
    public ResultEntity findByProjectId(@PathVariable(value = "projectId") String projectId){
        return ResultUtils.success(proDesignLinkService.findByProjectId(projectId));
    }

    // 保存子任务， 项目设置子任务(执行者，子任务，节点)
    @PatchMapping(value = "/byProject/{projectId}/setDesignLink")
    public ResultEntity setDesignLink(@PathVariable(value = "projectId") String projectId, String designLinkEntityId, String userId, String finishTime){
        return ResultUtils.success(proDesignLinkService.setProDesignLink(projectId, designLinkEntityId, userId, finishTime));
    }

    // 根据id查询子任务
    @GetMapping(value = "/{proDesignLinkId}")
    public ResultEntity getProDesignLinkById(String proDesignLinkById){
        return ResultUtils.success(proDesignLinkService.getProDesignLinkById(proDesignLinkById));
    }

    // 修改子任务(执行者，子任务，节点)
    @PatchMapping(value = "/{proDesignLinkById}/updateDesignLink")
    public ResultEntity updateProDesignLinkById(@PathVariable(value = "proDesignLinkById") String proDesignLinkById, String designLinkEntityId, String userId, String finishTime){
        return ResultUtils.success(proDesignLinkService.updateProDesignLinkById(proDesignLinkById, designLinkEntityId, userId, finishTime));
    }

    // 删除子任务
    @DeleteMapping(value = "/{proDesignLinkId}")
    public ResultEntity deleteProDesignLinkById(@PathVariable(value = "proDesignLinkId") String proDesignLinkId){
        return ResultUtils.success(proDesignLinkService.deleteProDesignLinkById(proDesignLinkId));
    }

    // 根据子任务id创建文件
    @PostMapping(value = "/{proDesignLinkId}/uploadfiles")
    public ResultEntity saveProDesignLinkFilesByProDesignId(@PathVariable(value = "proDesignLinkId") String proDesignLinkId, @RequestBody List<FileMetaEntity> fileMetaEntityList){
        return ResultUtils.success(proDesignLinkFilesService.saveProDesignLinkFilesByProDesignId(proDesignLinkId, fileMetaEntityList));
    }

    // 根据子任务id查询子任务下的文件
    @GetMapping(value = "/{proDesignLinkId}/files")
    public ResultEntity getProDesignLinkFilesByProDesignId(@PathVariable(value = "proDesignLinkId") String proDesignLinkId){
        return ResultUtils.success(proDesignLinkFilesService.getProDesignLinkFilesByProDesignId(proDesignLinkId));
    }

}
