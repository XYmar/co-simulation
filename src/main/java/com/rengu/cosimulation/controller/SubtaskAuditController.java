package com.rengu.cosimulation.controller;

import com.rengu.cosimulation.entity.Result;
import com.rengu.cosimulation.service.SubtaskAuditService;
import com.rengu.cosimulation.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

/**
 * Author: XYmar
 * Date: 2019/4/11 18:03
 */
@RestController
@RequestMapping(value = "/subtaskAudits")
public class SubtaskAuditController {
    private final SubtaskAuditService subtaskAuditService;

    @Autowired
    public SubtaskAuditController(SubtaskAuditService subtaskAuditService) {
        this.subtaskAuditService = subtaskAuditService;
    }

    // 根据用户id及子任务id查询该文件审核详情(当前的)
    @GetMapping(value = "/{subtaskId}")
    public Result getSublibraryFilesAuditId(@PathVariable(value = "subtaskId") String subtaskId, String subtaskDate) throws ParseException {
        return ResultUtils.success(subtaskAuditService.getSubtaskAudits(subtaskId, subtaskDate));
    }
}
