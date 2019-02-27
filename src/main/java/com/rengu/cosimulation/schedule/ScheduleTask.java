package com.rengu.cosimulation.schedule;

import com.rengu.cosimulation.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Author: XYmar
 * Date: 2019/2/27 14:26
 * 定时任务
 */
@Component
public class ScheduleTask {
    private final ProjectService projectService;

    @Autowired
    public ScheduleTask(ProjectService projectService) {
        this.projectService = projectService;
    }

    //  @Scheduled(fixedRate = 3 * 60 * 1000)   三分钟
    @Scheduled(cron = "0 30 10 28 * ?")        // 每月28号的10点30分触发
    public void reportCurrentTime() {
        projectService.deleteAllProject();
    }
}
