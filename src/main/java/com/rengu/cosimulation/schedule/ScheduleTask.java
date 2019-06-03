package com.rengu.cosimulation.schedule;

import com.rengu.cosimulation.service.ProjectService;
import com.rengu.cosimulation.utils.PreviewFileInit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Author: XYmar
 * Date: 2019/2/27 14:26
 * 定时任务
 */
@Component
@Slf4j
public class ScheduleTask {
    private final ProjectService projectService;
    @Value("${tmp.root}")
    private String rootPath;

    @Autowired
    public ScheduleTask(ProjectService projectService) {
        this.projectService = projectService;
    }

    //  @Scheduled(fixedRate = 3 * 60 * 1000)   三分钟
    @Scheduled(cron = "0 30 10 28 * ?")        // 每月28号的10点30分触发
    public void reportCurrentTime() {
        projectService.deleteAllProject();
    }

    @Autowired
    private PreviewFileInit previewFileInit;

    /**
     * @param
     * @Description: 每天凌晨一点 执行一次文件清理(清理临时存储文件)
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void removeFile() {
        File file=new File(rootPath);
        delFile(file);
    }
    static boolean delFile(File file) {
        if (!file.exists()) {
            return false;
        }
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                delFile(f);
            }
        }
        return file.delete();
    }
}
