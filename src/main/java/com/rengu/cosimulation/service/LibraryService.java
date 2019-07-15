package com.rengu.cosimulation.service;

import com.rengu.cosimulation.entity.Depot;
import com.rengu.cosimulation.enums.ResultCode;
import com.rengu.cosimulation.exception.ResultException;
import com.rengu.cosimulation.repository.LibraryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Author: XYmar
 * Date: 2019/3/29 16:12
 */
@Service
public class LibraryService {
    private final LibraryRepository libraryRepository;

    @Autowired
    public LibraryService(LibraryRepository libraryRepository) {
        this.libraryRepository = libraryRepository;
    }

    // 根据id查询库是否存在
    public boolean hasLibraryById(String id){
        if(StringUtils.isEmpty(id)){
            return false;
        }
        return libraryRepository.existsById(id);
    }

    // 根据id查询库
    public Depot getLibraryById(String id){
        if(!hasLibraryById(id)){
            throw new ResultException(ResultCode.LIBRARY_ID_NOT_FOUND_ERROR);
        }
        return libraryRepository.findById(id).get();
    }

    // 查询所有库
    public List<Depot> getAll(){
        return libraryRepository.findAll();
    }
}
