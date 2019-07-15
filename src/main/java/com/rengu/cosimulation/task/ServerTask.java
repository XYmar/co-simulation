package com.rengu.cosimulation.task;

import com.rengu.cosimulation.entity.Heartbeat;
import com.rengu.cosimulation.service.DeviceService;
import com.rengu.cosimulation.utils.ApplicationConfig;
import com.rengu.cosimulation.utils.IPUtils;
import com.rengu.cosimulation.utils.ServerCastUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InterfaceAddress;
import java.util.Iterator;
import java.util.Map;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-27 15:15
 **/

@Slf4j
@Component
public class ServerTask {

    // 周期发送心跳
    @Scheduled(fixedRate = 1000)
    public void serverCastTask() throws IOException {
        for (InterfaceAddress interfaceAddress : IPUtils.getLocalIPs()) {
            ServerCastUtils.sendMessage(interfaceAddress);
        }
    }

    // 检车设备在线状况
    @Scheduled(fixedRate = ApplicationConfig.HEART_BEAT_CHECK_TIME)
    public void onlineHostAdressCheck() {
        Iterator<Map.Entry<String, Heartbeat>> entryIterator = DeviceService.ONLINE_HOST_ADRESS.entrySet().iterator();
        while (entryIterator.hasNext()) {
            Map.Entry<String, Heartbeat> entry = entryIterator.next();
            Heartbeat heartbeatEntity = entry.getValue();
            if (heartbeatEntity.getCount() - 1 == 0) {
                entryIterator.remove();
                log.info(heartbeatEntity.getHostAddress() + "----->断开服务器连接。");
            } else {
                heartbeatEntity.setCount(heartbeatEntity.getCount() - 1);
            }
        }
    }
}
