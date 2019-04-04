package com.rengu.cosimulation.service;

import com.rengu.cosimulation.entity.SublibraryFilesAuditEntity;
import com.rengu.cosimulation.enums.ResultCode;
import com.rengu.cosimulation.exception.ResultException;
import com.rengu.cosimulation.repository.SublibraryFilesAuditRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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

    // 保存审核详情
   /* public SublibraryFilesAuditEntity save(SublibraryFilesAuditEntity sublibraryFilesAuditEntity, String userId, String sublibraryFileId){
        if(StringUtils.isEmpty(sublibraryFilesAuditEntity.isIfPass())){
            throw new ResultException(ResultCode.SUBLIBRARY_FILE_IFPASS_NOT_FOUND_ERROR);
        }
        if(StringUtils.isEmpty(userId)){
            throw new ResultException(ResultCode.USER_ID_NOT_FOUND_ERROR);
        }
        sublibraryFilesAuditEntity.setUserEntity(userService.getUserById(userId));             // 审核人
        return sublibraryFilesAuditRepository.save(sublibraryFilesAuditEntity);
    }*/
}
