package com.rengu.cosimulation.conventer;


import com.rengu.cosimulation.entity.PreviewFileEntity;
import com.rengu.cosimulation.utils.FileUtil;
import com.rengu.cosimulation.utils.PreviewFileInit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

/**
 * @Author YJH
 * @Date 2019/3/13 14:58
 */
@Component
public class ImageFileConventer {

    /**
     * 文件存储根目录
     */
    @Value("${tmp.root}")
    private String root;

    /**
     * 图片类型文件不转换,只更换存储目录 存储目录为 root/resource目录 + meta文件 + 源文件
     * resource目录存放转换后的文件，此处依然为源文件
     * meta文件存放文件基本信息
     *  @param previewFileEntity
     */
    public void conventer(PreviewFileEntity previewFileEntity) {
        // 创建hash目录
        String hashDirPath = root + File.separator + previewFileEntity.getFileId();
        File hashDir = FileUtil.createDir(hashDirPath);
        if (hashDir.exists() && hashDir.isDirectory()) {
            // 复制源文件到hash目录
            String filePath = previewFileEntity.getFilePath();
            FileUtil.copyFile(filePath, hashDirPath);
            // 计算文件大小
            previewFileEntity.setFileSize(FileUtil.getFileSize(filePath));
            // 创建resource目录，存放源文件
            String resourceDirPath = hashDirPath + File.separator + "resource";
            File resourceDir = FileUtil.createDir(resourceDirPath);
            if (resourceDir.exists() && resourceDir.isDirectory()) {
                FileUtil.copyFile(filePath, resourceDirPath);
               String filename= FileUtil.getFileName(filePath);
                previewFileEntity.setConventedFileName(filename);
            }
            // 创建meta文件，存放文件基本信息
            String metaPath = hashDirPath + File.separator + "meta";
            File metaFile = FileUtil.createFile(metaPath);
            FileUtil.writeContent(metaFile, previewFileEntity, "GBK");
        }
    }

}
