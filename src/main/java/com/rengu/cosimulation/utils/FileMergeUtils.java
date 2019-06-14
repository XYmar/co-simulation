package com.rengu.cosimulation.utils;

import com.rengu.cosimulation.entity.Chunk;
import com.rengu.cosimulation.enums.ResultCode;
import com.rengu.cosimulation.exception.ResultException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * author : yaojiahao
 * Date: 2019/6/14 18:34
 **/

@Slf4j
@Component
public class FileMergeUtils {

    @Async
    public CompletableFuture<File> megeChunks(int start, int end, Chunk chunk) throws IOException {
        long startTime = System.currentTimeMillis();
        File blockFile = new File(ApplicationConfig.CHUNKS_SAVE_PATH + java.io.File.separator + chunk.getIdentifier() + java.io.File.separator + start + ".block");
        blockFile.delete();
        blockFile.getParentFile().mkdirs();
        blockFile.createNewFile();
        for (int i = start; i <= end; i++) {
            File chunkFile = new File(ApplicationConfig.CHUNKS_SAVE_PATH + java.io.File.separator + chunk.getIdentifier() + java.io.File.separator + i + ".tmp");
            if (chunkFile.exists()) {
                FileUtils.writeByteArrayToFile(blockFile, FileUtils.readFileToByteArray(chunkFile), true);
            } else {
                throw new ResultException(ResultCode.FILE_CHUNK_NOT_FOUND_ERROR);
            }
        }
        log.info(chunk.getFilename() + "多线程合并，start：" + start + ",end:" + end + "结束,耗时：" + (System.currentTimeMillis() - startTime) + ",开始时间：" + startTime);
        return CompletableFuture.completedFuture(blockFile);
    }
}
