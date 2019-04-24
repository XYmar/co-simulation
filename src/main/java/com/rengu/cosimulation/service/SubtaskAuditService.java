package com.rengu.cosimulation.service;

import com.rengu.cosimulation.entity.SubtaskAuditEntity;
import com.rengu.cosimulation.entity.SubtaskEntity;
import com.rengu.cosimulation.enums.ResultCode;
import com.rengu.cosimulation.exception.ResultException;
import com.rengu.cosimulation.repository.SubtaskAuditRepository;
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
 * Date: 2019/4/11 17:54
 */
@Service
public class SubtaskAuditService {
    private final SubtaskAuditRepository subtaskAuditRepository;
    private final SubtaskService subtaskService;

    @Autowired
    public SubtaskAuditService(SubtaskAuditRepository subtaskAuditRepository, SubtaskService subtaskService) {
        this.subtaskAuditRepository = subtaskAuditRepository;
        this.subtaskService = subtaskService;
    }

    // 根据用户id及子任务id查询该文件审核详情(当前的)
    public List<SubtaskAuditEntity> getSubtaskAudits(String subtaskId, String subtaskDate) throws ParseException {
        SubtaskEntity subtaskEntity = subtaskService.getSubtaskById(subtaskId);
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = simpleDateFormat.parse(subtaskDate);
        return subtaskAuditRepository.findBySubtaskEntityAndCreateTimeAfter(subtaskEntity, date);
    }

    // 根据详情id查询子任务详情是否存在
    public boolean hasSubtaskById(String subtaskAuditId) {
        if (StringUtils.isEmpty(subtaskAuditId)) {
            return false;
        }
        return subtaskAuditRepository.existsById(subtaskAuditId);
    }

    // 根据id查询子任务文件
    @Cacheable(value = "SubtaskAudit_Cache", key = "#subtaskAuditId")
    public SubtaskAuditEntity getSubtaskAuditById(String subtaskAuditId) {
        if (!hasSubtaskById(subtaskAuditId)) {
            throw new ResultException(ResultCode.SUBTASK_ID_NOT_FOUND_ERROR);
        }
        return subtaskAuditRepository.findById(subtaskAuditId).get();
    }
}
