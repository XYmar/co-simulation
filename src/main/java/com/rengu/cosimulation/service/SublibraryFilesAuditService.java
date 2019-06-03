package com.rengu.cosimulation.service;

import com.rengu.cosimulation.entity.SubDepotFile;
import com.rengu.cosimulation.entity.SubDepotFileAudit;
import com.rengu.cosimulation.enums.ResultCode;
import com.rengu.cosimulation.exception.ResultException;
import com.rengu.cosimulation.repository.SublibraryFilesAuditRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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

    // 根据用户id及子库文件id查询该文件审核详情(当前的)
    public List<SubDepotFileAudit> getSublibraryFilesAudits(String sublibraryFileId, String sublibraryDate) throws ParseException {
        SubDepotFile subDepotFile = sublibraryFilesService.getSublibraryFileById(sublibraryFileId);
        // return sublibraryFilesAuditRepository.findBySubDepotFileAndIfOver(subDepotFile, false);
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = simpleDateFormat.parse(sublibraryDate);
        return sublibraryFilesAuditRepository.findBySubDepotFileAndCreateTimeAfter(subDepotFile, date);
    }

    // 根据详情id查询子库文件详情是否存在
    public boolean hasSublibraryFileById(String sublibraryFileAuditId) {
        if (StringUtils.isEmpty(sublibraryFileAuditId)) {
            return false;
        }
        return sublibraryFilesAuditRepository.existsById(sublibraryFileAuditId);
    }

    // 根据id查询子任务文件
    @Cacheable(value = "SublibraryFilesAudit_Cache", key = "#sublibraryFileAuditId")
    public SubDepotFileAudit getSublibraryFilesAuditById(String sublibraryFileAuditId) {
        if (!hasSublibraryFileById(sublibraryFileAuditId)) {
            throw new ResultException(ResultCode.SUBLIBRARY_FILE_ID_NOT_FOUND_ERROR);
        }
        return sublibraryFilesAuditRepository.findById(sublibraryFileAuditId).get();
    }

}
