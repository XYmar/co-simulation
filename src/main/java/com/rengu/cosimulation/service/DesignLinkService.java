package com.rengu.cosimulation.service;

import com.rengu.cosimulation.entity.DesignLink;
import com.rengu.cosimulation.enums.ResultCode;
import com.rengu.cosimulation.exception.ResultException;
import com.rengu.cosimulation.repository.DesignLinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Author: XYmar
 * Date: 2019/2/24 23:09
 */
@Service
public class DesignLinkService {
    private final DesignLinkRepository designLinkRepository;

    @Autowired
    public DesignLinkService(DesignLinkRepository designLinkRepository) {
        this.designLinkRepository = designLinkRepository;
    }

    // 设计环节新增
    @CacheEvict(value = "DesignLink_Cache", allEntries = true)
    public DesignLink saveDesignLink(DesignLink designLink){
        if(designLink == null){
            throw new ResultException(ResultCode.DESIGN_LINK_ARGS_NOT_FOUND_ERROR);
        }
        if(hasDesignLinkByName(designLink.getName())){
            throw new ResultException(ResultCode.DESIGN_LINK_NAME_EXISTED_ERROR);
        }
        return designLinkRepository.save(designLink);
    }

    // 根据名称查询设计环节是否存在
    public boolean hasDesignLinkByName(String name) {
        if(StringUtils.isEmpty(name)){
            return false;
        }

        return designLinkRepository.existsByName(name);
    }

    // 查询所有设计环节
    public List<DesignLink> getDesignLinks(){
        return designLinkRepository.findAll();
    }

    // 根据id查询设计环节
    @Cacheable(value = "DesignLink_Cache", key = "#designLinkId")
    public DesignLink getDesignLinkById(String designLinkId){
        if(!hasDesignLinkById(designLinkId)){
            throw new ResultException(ResultCode.DESIGN_LINK_ID_NOT_FOUND_ERROR);
        }
        return designLinkRepository.findById(designLinkId).get();
    }

    // 根据Id查询用户是否存在
    public boolean hasDesignLinkById(String designLinkId) {
        if (StringUtils.isEmpty(designLinkId)) {
            return false;
        }
        return designLinkRepository.existsById(designLinkId);
    }

    // 根据id修改设计环节
    @CachePut(value = "DesignLink_Cache", key = "#designLinkId")
    public DesignLink updateDesignLinkById(String designLinkId, DesignLink designLinkArgs){
        if(!hasDesignLinkById(designLinkId)){
            throw new ResultException(ResultCode.DESIGN_LINK_ID_NOT_FOUND_ERROR);
        }
        DesignLink designLink = getDesignLinkById(designLinkId);
        if(designLinkArgs == null){
            throw new ResultException(ResultCode.DESIGN_LINK_ARGS_NOT_FOUND_ERROR);
        }
        if(!StringUtils.isEmpty(designLinkArgs.getName()) && !designLink.getName().equals(designLinkArgs.getName())){
            if(hasDesignLinkByName(designLinkArgs.getName())){
                throw new ResultException(ResultCode.DESIGN_LINK_NAME_EXISTED_ERROR);
            }
            designLink.setName(designLinkArgs.getName());
        }
        designLink.setDescription(designLinkArgs.getDescription());
        if(!StringUtils.isEmpty(String.valueOf(designLinkArgs.getType()))){
            designLink.setType(designLinkArgs.getType());
        }
        return designLinkRepository.save(designLink);
    }

    // 根据id删除设计环节
    @CacheEvict(value = "DesignLink_Cache", key = "#designLinkId")
    public DesignLink deleteByDesignLinkId(String designLinkId) {
        if(!hasDesignLinkById(designLinkId)){
            throw new ResultException(ResultCode.DESIGN_LINK_ID_NOT_FOUND_ERROR);
        }
        DesignLink designLink = getDesignLinkById(designLinkId);
        designLinkRepository.delete(designLink);
        return designLink;
    }

}
