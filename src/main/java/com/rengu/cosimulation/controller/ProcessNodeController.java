package com.rengu.cosimulation.controller;

import com.rengu.cosimulation.entity.ProcessNodeEntity;
import com.rengu.cosimulation.entity.ResultEntity;
import com.rengu.cosimulation.service.ProcessNodeService;
import com.rengu.cosimulation.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * Author: XYmar
 * Date: 2019/3/7 14:54
 */
@RestController
@RequestMapping(value = "/processNodes")
public class ProcessNodeController {
    private final ProcessNodeService processNodeService;

    @Autowired
    public ProcessNodeController(ProcessNodeService processNodeService) {
        this.processNodeService = processNodeService;
    }

    // 保存项目流程节点信息
    /*@PostMapping
    public ResultEntity saveProcessNodes(@RequestHeader(value = "projectId") String projectId, @RequestBody @Valid  ProcessNodeEntity[] processNodeEntities){
        return ResultUtils.success(processNodeService.saveProcessNodes(projectId, processNodeEntities));
    }*/

    // 保存项目流程节点信息
    @PostMapping
    public ResultEntity saveProcessNodes(@RequestHeader(value = "projectId") String projectId, @RequestBody @Valid  ProcessNodeEntity[] processNodeEntities){
        return ResultUtils.success(processNodeService.saveProcessNodes(projectId, processNodeEntities));
    }

}
