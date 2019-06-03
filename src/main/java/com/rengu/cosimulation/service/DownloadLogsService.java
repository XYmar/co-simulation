package com.rengu.cosimulation.service;

import com.rengu.cosimulation.entity.DownloadLog;
import com.rengu.cosimulation.repository.DownloadLogsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Author: XYmar
 * Date: 2019/5/5 13:22
 */
@Service
public class DownloadLogsService {
    private final DownloadLogsRepository downloadLogsRepository;

    @Autowired
    public DownloadLogsService(DownloadLogsRepository downloadLogsRepository) {
        this.downloadLogsRepository = downloadLogsRepository;
    }

    // 获取下载日志信息
    public List<DownloadLog> getLogs(){
        return downloadLogsRepository.findAll();
    }
}
