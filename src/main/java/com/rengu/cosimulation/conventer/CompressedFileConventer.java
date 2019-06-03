package com.rengu.cosimulation.conventer;


import com.rengu.cosimulation.entity.PreviewFile;
import com.rengu.cosimulation.utils.FileUtil;
import com.rengu.cosimulation.utils.ZipUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @Author YJH
 * @Date 2019/3/13 14:58
 */
@Component
public class CompressedFileConventer {

    @Value("${tmp.root}")
    private String root;

    public void conventer(PreviewFile previewFile) {

        // 创建hash目录
        String hashDirPath = root + File.separator + previewFile.getFileId();
        File hashDir = FileUtil.createDir(hashDirPath);
        if (hashDir.exists() && hashDir.isDirectory()) {
            // 复制源文件到hash目录
            String filePath = previewFile.getFilePath();
            FileUtil.copyFile(filePath, hashDirPath);
            // 计算文件大小
            previewFile.setFileSize(FileUtil.getFileSize(filePath));
            // 创建resource目录，存放源文件
            String resourceDirPath = hashDirPath + File.separator + "resource";
            File resourceDir = FileUtil.createDir(resourceDirPath);
            if (resourceDir.exists() && resourceDir.isDirectory()) {
                // 压缩文件解压到resource目录下
                String fileTree = "";
                try {
                    // 解压文件并获取文件列表
                    fileTree = ZipUtil.unCompress(filePath, resourceDirPath);
                    fileTree = fileTree.replace(root, "");
                }catch (Exception e) {
                    e.printStackTrace();
                }
                previewFile.setFileTree(fileTree);
                int splitIndex = previewFile.getFilePath().lastIndexOf(".");
                String filename= FileUtil.getFileName(previewFile.getFilePath().substring(0,splitIndex));
                previewFile.setConventedFileName(filename);
            }
            // 创建meta文件，存放文件基本信息
            String metaPath = hashDirPath + File.separator + "meta";
            File metaFile = FileUtil.createFile(metaPath);
            FileUtil.writeContent(metaFile, previewFile, "GBK");
        }

    }
}
