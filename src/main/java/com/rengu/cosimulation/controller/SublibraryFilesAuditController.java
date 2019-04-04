package com.rengu.cosimulation.controller;

import com.rengu.cosimulation.entity.ResultEntity;
import com.rengu.cosimulation.service.SublibraryFilesAuditService;
import com.rengu.cosimulation.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Author: XYmar
 * Date: 2019/4/4 15:36
 */
@RestController
@RequestMapping(value = "/sublibraryFilesAudits")
public class SublibraryFilesAuditController {
    private final SublibraryFilesAuditService sublibraryFilesAuditService;

    @Autowired
    public SublibraryFilesAuditController(SublibraryFilesAuditService sublibraryFilesAuditService) {
        this.sublibraryFilesAuditService = sublibraryFilesAuditService;
    }

    @GetMapping(value = "/{sublibraryFileId}")
    public ResultEntity getSublibraryFilesAuditId(@PathVariable(value = "sublibraryFileId") String sublibraryFileId){
        return ResultUtils.success(sublibraryFilesAuditService.getSublibraryFilesAudits(sublibraryFileId));
    }
}
