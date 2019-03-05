package com.rengu.cosimulation.service;

import com.rengu.cosimulation.entity.FileEntity;
import com.rengu.cosimulation.entity.FileMetaEntity;
import com.rengu.cosimulation.entity.ProDesignLinkEntity;
import com.rengu.cosimulation.entity.ProDesignLinkFilesEntity;
import com.rengu.cosimulation.enums.ResultCode;
import com.rengu.cosimulation.exception.ResultException;
import com.rengu.cosimulation.repository.ProDesignLinkFilesRepository;
import com.rengu.cosimulation.repository.ProDesignLinkRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Author: XYmar
 * Date: 2019/3/4 12:56
 */
@Service
@Slf4j
public class ProDesignLinkFilesService {
    private final ProDesignLinkService proDesignLinkService;
    private final ProDesignLinkFilesRepository proDesignLinkFilesRepository;
    private final FileService fileService;

    @Autowired
    public ProDesignLinkFilesService(FileService fileService, ProDesignLinkFilesRepository proDesignLinkFilesRepository, ProDesignLinkService proDesignLinkService) {
        this.fileService = fileService;
        this.proDesignLinkFilesRepository = proDesignLinkFilesRepository;
        this.proDesignLinkService = proDesignLinkService;
    }

    // 根据名称、后缀及子任务检查文件是否存在
    public boolean hasProDesignLinkFilesByNameAndExtensionAndProDesignLink(String name, String postfix, ProDesignLinkEntity proDesignLinkEntity) {
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(postfix)) {
            return false;
        }
        return proDesignLinkFilesRepository.existsByNameAndPostfixAndProDesignLinkEntity(name, postfix, proDesignLinkEntity);
    }

    // 根据名称、后缀及子任务查询文件
    public ProDesignLinkFilesEntity getProDesignLinkFilesByNameAndPostfixAndProDesignLink(String name, String postfix, ProDesignLinkEntity proDesignLinkEntity) {
        return proDesignLinkFilesRepository.findByNameAndPostfixAndProDesignLinkEntity(name, postfix, proDesignLinkEntity).get();
    }

    // 根据子任务id创建文件
    @CacheEvict(value = "ProDesignLinkFiles_Cache", allEntries = true)
    public List<ProDesignLinkFilesEntity> saveProDesignLinkFilesByProDesignId(String proDesignLinkId, List<FileMetaEntity> fileMetaEntityList) {
        if(!proDesignLinkService.hasProDesignLinkById(proDesignLinkId)){
            throw new ResultException(ResultCode.PRODESIGN_LINK_ID_NOT_FOUND_ERROR);
        }
        ProDesignLinkEntity proDesignLinkEntity = proDesignLinkService.getProDesignLinkById(proDesignLinkId);
        List<ProDesignLinkFilesEntity> proDesignLinkFilesEntityList = new ArrayList<>();
        for (FileMetaEntity fileMetaEntity : fileMetaEntityList) {
            String path = fileMetaEntity.getRelativePath().split("/")[1];

            // 判断该节点是否存在
            if (hasProDesignLinkFilesByNameAndExtensionAndProDesignLink(FilenameUtils.getBaseName(path), FilenameUtils.getExtension(path), proDesignLinkEntity)) {
                ProDesignLinkFilesEntity proDesignLinkFilesEntity = getProDesignLinkFilesByNameAndPostfixAndProDesignLink(FilenameUtils.getBaseName(path), FilenameUtils.getExtension(path), proDesignLinkEntity);
                proDesignLinkFilesEntity.setCreateTime(new Date());
                proDesignLinkFilesEntity.setName(FilenameUtils.getBaseName(fileMetaEntity.getRelativePath()));
                proDesignLinkFilesEntity.setPostfix(FilenameUtils.getExtension(fileMetaEntity.getRelativePath()));
                proDesignLinkFilesEntity.setType(fileMetaEntity.getType());
                proDesignLinkFilesEntity.setSecretClass(fileMetaEntity.getSecretClass());
                proDesignLinkFilesEntity.setCodeName(fileMetaEntity.getCodeName());
                proDesignLinkFilesEntity.setVersion(1);
                proDesignLinkFilesEntity.setFileEntity(fileService.getFileById(fileMetaEntity.getFileId()));
                proDesignLinkFilesEntityList.add(proDesignLinkFilesRepository.save(proDesignLinkFilesEntity));
            } else {
                ProDesignLinkFilesEntity proDesignLinkFilesEntity = new ProDesignLinkFilesEntity();
                proDesignLinkFilesEntity.setName(StringUtils.isEmpty(FilenameUtils.getBaseName(fileMetaEntity.getRelativePath())) ? "-" : FilenameUtils.getBaseName(fileMetaEntity.getRelativePath()));
                proDesignLinkFilesEntity.setPostfix(FilenameUtils.getExtension(fileMetaEntity.getRelativePath()));
                proDesignLinkFilesEntity.setType(fileMetaEntity.getType());
                proDesignLinkFilesEntity.setSecretClass(fileMetaEntity.getSecretClass());
                proDesignLinkFilesEntity.setCodeName(fileMetaEntity.getCodeName());
                proDesignLinkFilesEntity.setVersion(1);
                proDesignLinkFilesEntity.setFileEntity(fileService.getFileById(fileMetaEntity.getFileId()));
                proDesignLinkFilesEntity.setProDesignLinkEntity(proDesignLinkEntity);
                proDesignLinkFilesEntityList.add(proDesignLinkFilesRepository.save(proDesignLinkFilesEntity));
            }
        }
        return proDesignLinkFilesEntityList;
    }

}
