package com.rengu.cosimulation.service;

import com.rengu.cosimulation.entity.SublibraryFilesAuditEntity;
import com.rengu.cosimulation.entity.SublibraryFilesEntity;
import com.rengu.cosimulation.entity.SubtaskFilesEntity;
import com.rengu.cosimulation.entity.UserEntity;
import com.rengu.cosimulation.enums.ResultCode;
import com.rengu.cosimulation.exception.ResultException;
import com.rengu.cosimulation.repository.SublibraryFilesAuditRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Author: XYmar
 * Date: 2019/4/4 10:47
 */
@Service
public class SublibraryFilesAuditService {
    private final SublibraryFilesAuditRepository sublibraryFilesAuditRepository;
    private final UserService userService;
    private final SublibraryFilesService sublibraryFilesService;

    @Autowired
    public SublibraryFilesAuditService(SublibraryFilesAuditRepository sublibraryFilesAuditRepository, UserService userService, SublibraryFilesService sublibraryFilesService) {
        this.sublibraryFilesAuditRepository = sublibraryFilesAuditRepository;
        this.userService = userService;
        this.sublibraryFilesService = sublibraryFilesService;
    }

    // 根据用户id及子库文件id查询该文件审核详情
    public List<SublibraryFilesAuditEntity> getSublibraryFilesAudits(String sublibraryFileId, String userId){
        UserEntity userEntity = userService.getUserById(userId);
        SublibraryFilesEntity sublibraryFilesEntity = sublibraryFilesService.getSublibraryFileById(sublibraryFileId);
        return sublibraryFilesAuditRepository.findBySublibraryFilesEntityAndUserEntity(sublibraryFilesEntity, userEntity);
    }

    // 根据id查询子任务文件是否存在
    public boolean hasSublibraryFileById(String sublibraryFileAuditId) {
        if (StringUtils.isEmpty(sublibraryFileAuditId)) {
            return false;
        }
        return sublibraryFilesAuditRepository.existsById(sublibraryFileAuditId);
    }

    // 根据id查询子任务文件
    @Cacheable(value = "SublibraryFile_Cache", key = "#sublibraryFileAuditId")
    public SublibraryFilesAuditEntity getSublibraryFilesAuditById(String sublibraryFileAuditId) {
        if (!hasSublibraryFileById(sublibraryFileAuditId)) {
            throw new ResultException(ResultCode.SUBLIBRARY_FILE_ID_NOT_FOUND_ERROR);
        }
        return sublibraryFilesAuditRepository.findById(sublibraryFileAuditId).get();
    }

}
