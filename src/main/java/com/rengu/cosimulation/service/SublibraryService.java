package com.rengu.cosimulation.service;

import com.rengu.cosimulation.entity.Depot;
import com.rengu.cosimulation.entity.SubDepot;
import com.rengu.cosimulation.enums.ResultCode;
import com.rengu.cosimulation.exception.ResultException;
import com.rengu.cosimulation.repository.SubLibraryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Author: XYmar
 * Date: 2019/3/29 16:09
 */
@Service
public class SublibraryService {
    private final SubLibraryRepository subLibraryRepository;
    private final LibraryService libraryService;

    @Autowired
    public SublibraryService(SubLibraryRepository subLibraryRepository, LibraryService libraryService) {
        this.subLibraryRepository = subLibraryRepository;
        this.libraryService = libraryService;
    }

    // 根据库id查询子库
    public List<SubDepot> getSublibrariesByLibraryId(String libraryId){
        if(!libraryService.hasLibraryById(libraryId)){
            throw new ResultException(ResultCode.LIBRARY_ID_NOT_FOUND_ERROR);
        }
        Depot depot = libraryService.getLibraryById(libraryId);
        return subLibraryRepository.findByDepot(depot);
    }

    // 根据id查询子库是否存在
    public boolean hasSublibraryById(String id){
        if(StringUtils.isEmpty(id)){
            return false;
        }
        return subLibraryRepository.existsById(id);
    }

    // 根据id查询子库
    public SubDepot getSublibraryById(String id){
        if(!hasSublibraryById(id)){
            throw new ResultException(ResultCode.SUBLIBRARY_ID_NOT_FOUND_ERROR);
        }
        return subLibraryRepository.findById(id).get();
    }

    // 新增子库
    public SubDepot saveSublibrary(SubDepot subDepot, String libraryId){
        if(!libraryService.hasLibraryById(libraryId)){
            throw new ResultException(ResultCode.LIBRARY_ID_NOT_FOUND_ERROR);
        }
        if(hasSublibraryByType(subDepot.getType())){
            throw new ResultException(ResultCode.SUBLIBRARY_TYPE_EXISTED_ERROR);
        }
        Depot depot = libraryService.getLibraryById(libraryId);
        subDepot.setDepot(depot);
        return subLibraryRepository.save(subDepot);
    }

    // 删除子库
    public SubDepot deleteSublibraryById(String id){
        if(!hasSublibraryById(id)){
            throw new ResultException(ResultCode.SUBLIBRARY_ID_NOT_FOUND_ERROR);
        }
        SubDepot subDepot = getSublibraryById(id);
        subLibraryRepository.delete(subDepot);
        return subDepot;
    }

    // 修改子库
    public SubDepot updateSublibraryById(String id, SubDepot subDepotArgs){
        if(!hasSublibraryById(id)){
            throw new ResultException(ResultCode.SUBLIBRARY_ID_NOT_FOUND_ERROR);
        }
        SubDepot subDepot = getSublibraryById(id);
        if(!StringUtils.isEmpty(subDepotArgs.getType()) && !subDepot.getType().equals(subDepotArgs.getType())){
            if(hasSublibraryByType(subDepotArgs.getType())){
                throw new ResultException(ResultCode.SUBLIBRARY_TYPE_EXISTED_ERROR);
            }
            subDepot.setType(subDepotArgs.getType());
        }
        if(!StringUtils.isEmpty(subDepotArgs.getDescription())){
            subDepot.setDescription(subDepotArgs.getDescription());
        }
        return subLibraryRepository.save(subDepot);
    }

    // 根据类型判断子库是否存在
    public boolean hasSublibraryByType(String type){
        if(StringUtils.isEmpty(type)){
           return false;
        }
        return subLibraryRepository.existsByType(type);
    }
}
