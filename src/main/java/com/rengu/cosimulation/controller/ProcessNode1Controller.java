package com.rengu.cosimulation.controller;

import com.rengu.cosimulation.entity.ProcessNode;
import com.rengu.cosimulation.entity.Result;
import com.rengu.cosimulation.service.ProcessNode1Service;
import com.rengu.cosimulation.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * Author: XYmar
 * Date: 2019/4/17 19:43
 */
@RestController
@RequestMapping(value = "/processNode1")
public class ProcessNode1Controller {
    private final ProcessNode1Service processNode1Service;

    @Autowired
    public ProcessNode1Controller(ProcessNode1Service processNode1Service) {
        this.processNode1Service = processNode1Service;
    }

    // 保存项目流程节点信息
    @PostMapping
    public Result saveProcessNodes(@RequestHeader(value = "projectId") String projectId, @RequestBody @Valid ProcessNode[] processNodeEntities){
        return ResultUtils.success(processNode1Service.saveProcessNodes(projectId, processNodeEntities));
    }

    // 根据项目返回流程节点信息
    @GetMapping(value = "/byProjectId/{projectId}")
    public Result getProcessNodesByProjectId(@PathVariable(value = "projectId") String projectId){
        return ResultUtils.success(processNode1Service.getProcessNodesByProjectId(projectId));
    }

    // 项目是否已经包含项目流程
    @PostMapping("/ifHasProcessNode")
    public Result ifHasProcessNode(String projectId){
        return ResultUtils.success(processNode1Service.ifHasProcessNode(projectId));
    }
}
