package com.rengu.cosimulation.service;

import com.rengu.cosimulation.entity.LibraryEntity;
import com.rengu.cosimulation.entity.SublibraryEntity;
import com.rengu.cosimulation.enums.ResultCode;
import com.rengu.cosimulation.exception.ResultException;
import com.rengu.cosimulation.repository.SubLibraryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Author: XYmar
 * Date: 2019/3/29 16:09
 */
@Service
public class SubLibraryService {
    private final SubLibraryRepository subLibraryRepository;
    private final LibraryService libraryService;

    @Autowired
    public SubLibraryService(SubLibraryRepository subLibraryRepository, LibraryService libraryService) {
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

}
