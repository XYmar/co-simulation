package com.rengu.cosimulation.service;

import com.rengu.cosimulation.entity.LibraryEntity;
import com.rengu.cosimulation.entity.SublibraryEntity;
import com.rengu.cosimulation.enums.ResultCode;
import com.rengu.cosimulation.exception.ResultException;
import com.rengu.cosimulation.repository.SubLibraryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.xml.transform.Result;
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
    public List<SublibraryEntity> getSublibrariesByLibraryId(String libraryId){
        if(!libraryService.hasLibraryById(libraryId)){
            throw new ResultException(ResultCode.LIBRARY_ID_NOT_FOUND_ERROR);
        }
        LibraryEntity libraryEntity = libraryService.getLibraryById(libraryId);
        return subLibraryRepository.findByLibraryEntity(libraryEntity);
    }

    // 根据id查询子库是否存在
    public boolean hasSublibraryById(String id){
        if(StringUtils.isEmpty(id)){
            return false;
        }
        return subLibraryRepository.existsById(id);
    }

    // 根据id查询子库
    public SublibraryEntity getSublibraryById(String id){
        if(!hasSublibraryById(id)){
            throw new ResultException(ResultCode.SUBLIBRARY_ID_NOT_FOUND_ERROR);
        }
        return subLibraryRepository.findById(id).get();
    }

    // 新增子库
    public SublibraryEntity saveSublibrary(SublibraryEntity sublibraryEntity,String libraryId){
        if(!libraryService.hasLibraryById(libraryId)){
            throw new ResultException(ResultCode.LIBRARY_ID_NOT_FOUND_ERROR);
        }
        if(hasSublibraryByType(sublibraryEntity.getType())){
            throw new ResultException(ResultCode.SUBLIBRARY_TYPE_EXISTED_ERROR);
        }
        LibraryEntity libraryEntity = libraryService.getLibraryById(libraryId);
        sublibraryEntity.setLibraryEntity(libraryEntity);
        return subLibraryRepository.save(sublibraryEntity);
    }

    // 删除子库
    public SublibraryEntity deleteSublibraryById(String id){
        if(!hasSublibraryById(id)){
            throw new ResultException(ResultCode.SUBLIBRARY_ID_NOT_FOUND_ERROR);
        }
        SublibraryEntity sublibraryEntity = getSublibraryById(id);
        subLibraryRepository.delete(sublibraryEntity);
        return sublibraryEntity;
    }

    // 修改子库
    public SublibraryEntity updateSublibraryById(String id, SublibraryEntity sublibraryEntityArgs){
        if(!hasSublibraryById(id)){
            throw new ResultException(ResultCode.SUBLIBRARY_ID_NOT_FOUND_ERROR);
        }
        SublibraryEntity sublibraryEntity = getSublibraryById(id);
        if(!StringUtils.isEmpty(sublibraryEntityArgs.getType()) && !sublibraryEntity.getType().equals(sublibraryEntityArgs.getType())){
            if(hasSublibraryByType(sublibraryEntityArgs.getType())){
                throw new ResultException(ResultCode.SUBLIBRARY_TYPE_EXISTED_ERROR);
            }
            sublibraryEntity.setType(sublibraryEntityArgs.getType());
        }
        if(!StringUtils.isEmpty(sublibraryEntityArgs.getDescription())){
            sublibraryEntity.setDescription(sublibraryEntityArgs.getDescription());
        }
        return subLibraryRepository.save(sublibraryEntity);
    }

    // 根据类型判断子库是否存在
    public boolean hasSublibraryByType(String type){
        if(StringUtils.isEmpty(type)){
           return false;
        }
        return subLibraryRepository.existsByType(type);
    }
}
