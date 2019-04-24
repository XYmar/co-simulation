package com.rengu.cosimulation.controller;

import com.rengu.cosimulation.entity.LinkEntity;
import com.rengu.cosimulation.entity.ResultEntity;
import com.rengu.cosimulation.service.LinkService;
import com.rengu.cosimulation.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * Author: XYmar
 * Date: 2019/4/17 20:07
 */
@RestController
@RequestMapping(value = "/links")
public class LinkController {
    private final LinkService linkService;

    @Autowired
    public LinkController(LinkService linkService) {
        this.linkService = linkService;
    }

    // 保存项目流程节点信息
    @PostMapping
    public ResultEntity saveLinks(@RequestHeader(value = "projectId") String projectId, @RequestBody @Valid LinkEntity[] linkEntities){
        return ResultUtils.success(linkService.saveLinks(projectId, linkEntities));
    }

}
