package com.rengu.cosimulation.conventer;

import com.rengu.cosimulation.entity.PreviewFile;
import com.rengu.cosimulation.utils.FileCharsetDetector;
import com.rengu.cosimulation.utils.FileUtil;
import org.mozilla.intl.chardet.nsPSMDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @Author YJH
 * @Date 2019/3/13 14:58
 */
@Component
public class TextFileConventer {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${tmp.root}")
    private String root;
    @Value("${tmp.rootTemp}")
    private String rootTemp;

    /**
     * txt文件转换后更改文件编码格式为utf-8
     *
     * @param previewFile
     */
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
                // 纯文本,涉及到文件编码问题
                try {
                    File original = new File(previewFile.getFilePath());
                    FileCharsetDetector.Observer oCharset = FileCharsetDetector.guessFileEncoding(
                            original, nsPSMDetector.CHINESE);
                    String charset = null;
                    if (oCharset.isFound()) {
                        // 探测到编码
                        charset = oCharset.getEncoding();
                    } else if (oCharset.getEncoding() != null
                            && !oCharset.isFound()) {
                        // 猜测到编码
                        logger.error("Doc2PdfServiceImpl@convert error:"
                                + previewFile.getFilePath()
                                + ",use the guess charset:"
                                + oCharset.getEncoding());
                        charset = oCharset.getEncoding();
                    } else {
                        // 未找到编码,大部分情况 文档都是来自同一个window系统,使用GBK
                        logger.error("Doc2PdfServiceImpl@convert error:"
                                + previewFile.getFilePath()
                                + ",can't find the charset.use :GBK");
                        charset = "GBK";
                    }

                    // 再进纯文本一律先转化为UTF-8的txt文件行转码
                    String outFile = resourceDirPath + File.separator
                            + FileUtil.getFileName(previewFile.getFilePath())
                            + "-utf8.txt";
                    // 将文本文件复制到resource目录
                    FileUtil.copyFile(filePath, charset, outFile, "GBK");
                    String filename = FileUtil.getFileName(filePath);
                    previewFile.setConventedFileName(filename + "-utf8.txt");
                    previewFile.setOriginalMIMEType("text/plain");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // 创建meta文件，存放文件基本信息
                String metaPath = hashDirPath + File.separator + "meta";
                File metaFile = FileUtil.createFile(metaPath);
                FileUtil.writeContent(metaFile, previewFile, "GBK");
            }
        }
    }
}
