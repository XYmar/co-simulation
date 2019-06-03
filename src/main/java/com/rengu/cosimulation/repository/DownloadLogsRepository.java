package com.rengu.cosimulation.repository;

import com.rengu.cosimulation.entity.DownloadLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Author: XYmar
 * Date: 2019/4/22 9:26
 */
@Repository
public interface DownloadLogsRepository extends JpaRepository<DownloadLog, String> {
}
