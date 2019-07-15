package com.rengu.cosimulation.service;

import com.rengu.cosimulation.entity.DownloadLog;
import com.rengu.cosimulation.entity.SubDepotFile;
import com.rengu.cosimulation.entity.SubdepotFileHis;
import com.rengu.cosimulation.entity.Users;
import com.rengu.cosimulation.enums.ResultCode;
import com.rengu.cosimulation.exception.ResultException;
import com.rengu.cosimulation.repository.DownloadLogsRepository;
import com.rengu.cosimulation.repository.SublibraryFilesHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;

/**
 * Author: XYmar
 * Date: 2019/7/2 16:04
 */
@Service
@Slf4j
public class SublibraryFilesHisService {
    private final SublibraryFilesHistoryRepository sublibraryFilesHistoryRepository;
    private final UserService userService;
    private final DownloadLogsRepository downloadLogsRepository;

    @Autowired
    public SublibraryFilesHisService(SublibraryFilesHistoryRepository sublibraryFilesHistoryRepository, UserService userService, DownloadLogsRepository downloadLogsRepository) {
        this.sublibraryFilesHistoryRepository = sublibraryFilesHistoryRepository;
        this.userService = userService;
        this.downloadLogsRepository = downloadLogsRepository;
    }

    // 根据子库文件的id下载文件
    public File exportSublibraryFileHisById(String sublibraryFileId, String userId) throws IOException {
        if(!userService.hasUserById(userId)){
            throw new ResultException(ResultCode.USER_ID_NOT_FOUND_ERROR);
        }
        Users users = userService.getUserById(userId);
        int userSecretClass = users.getSecretClass();     //获取用户密级
        if(!hasSublibraryFileHisById(sublibraryFileId)){
            throw new ResultException(ResultCode.SUBLIBRARY_FILE_ID_NOT_FOUND_ERROR);
        }
        int fileSecretClass = getSublibraryFileHisById(sublibraryFileId).getSecretClass();
        // 用户只能下载小于等于自己密级的文件
        if(userSecretClass < fileSecretClass){
            throw new ResultException(ResultCode.SUBLIBRARY_FILE_DOWNLOAD_DENIED_ERROR);
        }
        SubdepotFileHis subdepotFileHis = getSublibraryFileHisById(sublibraryFileId);
        File exportFile = new File(FileUtils.getTempDirectoryPath() + File.separator + subdepotFileHis.getName() + "." + subdepotFileHis.getFiles().getPostfix());
        FileUtils.copyFile(new File(subdepotFileHis.getFiles().getLocalPath()), exportFile);
        DownloadLog downloadLog = new DownloadLog();
        downloadLog.setFileName(subdepotFileHis.getName());
        downloadLog.setUsers(users);
        downloadLog.setFiles(subdepotFileHis.getFiles());
        downloadLogsRepository.save(downloadLog);
        return exportFile;
    }

    // 根据id查询子库文件是否存在
    public boolean hasSublibraryFileHisById(String sublibraryFileHisId) {
        if (StringUtils.isEmpty(sublibraryFileHisId)) {
            return false;
        }
        return sublibraryFilesHistoryRepository.existsById(sublibraryFileHisId);
    }

    // 根据文件id查询子库文件
    public SubdepotFileHis getSublibraryFileHisById(String sublibraryFileHisId) {
        if (!hasSublibraryFileHisById(sublibraryFileHisId)) {
            throw new ResultException(ResultCode.SUBLIBRARY_ID_NOT_FOUND_ERROR);
        }
        return sublibraryFilesHistoryRepository.findById(sublibraryFileHisId).get();
    }
}
