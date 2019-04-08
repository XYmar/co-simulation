package com.rengu.cosimulation.controller;

import com.rengu.cosimulation.entity.DesignLinkEntity;
import com.rengu.cosimulation.entity.ResultEntity;
import com.rengu.cosimulation.enums.ResultCode;
import com.rengu.cosimulation.exception.ResultException;
import com.rengu.cosimulation.service.DesignLinkService;
import com.rengu.cosimulation.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * Author: XYmar
 * Date: 2019/2/25 14:22
 */
@RestController
@RequestMapping(value = "/designLinks")
public class DesignLinkController {
    @Autowired
    private DesignLinkService designLinkService;

    // 新增设计环节(名称)
    // TODO 权限控制（谁可以新增）
    @PostMapping
    public ResultEntity saveDesignLink(@RequestBody @Valid DesignLinkEntity designLinkEntity){
        if(designLinkEntity == null){
            throw new ResultException(ResultCode.DESIGN_LINK_ARGS_NOT_FOUND_ERROR);
        }
        if(StringUtils.isEmpty(designLinkEntity.getName())){
            throw new ResultException(ResultCode.DESIGN_LINK_NAME_ARGS_NOT_FOUND_ERROR);
        }
        return ResultUtils.success(designLinkService.saveDesignLink(designLinkEntity));
    }

    // 查询设计环节
    @GetMapping
    public ResultEntity getDesignLinks(){
        return ResultUtils.success(designLinkService.getDesignLinks());
    }

    // 根据id修改设计环节
    @PatchMapping(value = "/{designLinkId}")
    public ResultEntity updateDesignLinkById(@PathVariable(value = "designLinkId") String designLinkId, DesignLinkEntity designLinkEntity){
        return ResultUtils.success(designLinkService.updateDesignLinkById(designLinkId, designLinkEntity));
    }

    // 根据id删除设计环节
    @DeleteMapping(value = "/{designLinkId}")
    public ResultEntity deleteDesignLinkById(@PathVariable(value = "designLinkId") String designLinkId){
        return ResultUtils.success(designLinkService.deleteByDesignLinkId(designLinkId));
    }
}
