package com.rengu.cosimulation.conventer;

import com.rengu.cosimulation.entity.PreviewFile;
import com.rengu.cosimulation.utils.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;

/**
 * @Author YJH
 * @Date 2019/3/13 14:58
 */
@Component
@Slf4j
public class OfficeFileConventer {


    @Value("${tmp.root}")
    private String root;

    @Value("${soffice.home}")
    private String officeHome;

    private static int[] port = { 8100 };

    /**
     * @Fields officeManager : openoffice管理器
     */
    private OfficeManager officeManager;

    /**
     * office文件统一转为html格式文件
     *
     * @param previewFile
     */
    public void conventerToHtml(PreviewFile previewFile) {

        // 创建hash目录
        String hashDirPath = root + File.separator + previewFile.getFileId();
        log.info(hashDirPath);
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
                // 进行文件转换
                String fileName = FileUtil
                        .getFileName(FileUtil.getFileName(previewFile.getFilePath()));
                String htmlFilePath = fileName + ".html";
                String inputFile = previewFile.getFilePath();
                // 转换后的文件放在resource目录中
                String outputFile = resourceDirPath + File.separator
                        + htmlFilePath;
                log.info("进行文档转换:" + inputFile + " --> " + outputFile);
                OfficeDocumentConverter converter = new OfficeDocumentConverter(this.officeManager);
                File input = new File(inputFile);
                File html = new File(outputFile);
                converter.convert(input, html);
               previewFile.setConventedFileName(htmlFilePath);
                // 设置content-type
                previewFile.setOriginalMIMEType("text/html");
            }
            // 创建meta文件，存放文件基本信息
            String metaPath = hashDirPath + File.separator + "meta";
            File metaFile = FileUtil.createFile(metaPath);
            FileUtil.writeContent(metaFile, previewFile, "GBK");
        }
    }

    /**
     * office文件转为pdf格式
     */
    public void conventerToPdf(PreviewFile previewFile) {

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
                // 进行文件转换
                String fileName = FileUtil
                        .getFileName(previewFile.getFilePath());
                String htmlFilePath = fileName + ".pdf";
                String inputFile = previewFile.getFilePath();
                // 转换后的文件放在resource目录中
                String outputFile = resourceDirPath + File.separator
                        + htmlFilePath;
                this.log.info("进行文档转换:" + inputFile + " --> " + outputFile);
                OfficeDocumentConverter converter = new OfficeDocumentConverter(this.officeManager);
                File input = new File(inputFile);
                File html = new File(outputFile);
                converter.convert(input, html);
                previewFile.setConventedFileName(htmlFilePath);
                // 设置content-type
                previewFile.setOriginalMIMEType("application/pdf");
            }
            // 创建meta文件，存放文件基本信息
            String metaPath = hashDirPath + File.separator + "meta";
            File metaFile = FileUtil.createFile(metaPath);
            FileUtil.writeContent(metaFile, previewFile, "GBK");
        }
    }

    @PostConstruct
    public void init() {
        // 开启openoffice服务
        System.out.println("开启服务");
        startService();
    }

    @PreDestroy
    public void destroy() {
        System.out.println("关闭服务");
        stopService();
    }

    private void startService() {
        DefaultOfficeManagerConfiguration configuration = new DefaultOfficeManagerConfiguration();
        this.log.warn("start openoffice....");

        // 设置OpenOffice.org安装目录
        configuration.setOfficeHome(officeHome);
        // 设置转换端口，默认为8100
        configuration.setPortNumbers(port);
        // 设置任务执行超时为5分钟
        configuration.setTaskExecutionTimeout(1000 * 60 * 5L);
        // 设置任务队列超时为24小时
        configuration.setTaskQueueTimeout(1000 * 60 * 60 * 24L);
        officeManager = configuration.buildOfficeManager();
        officeManager.start(); // 启动服务
        this.log.warn("openoffice start success!");
    }

    private void stopService() {
        this.log.warn("stop openoffice...");
        if (officeManager != null) {
            officeManager.stop();
        }
        this.log.warn("stop openoffice success!");
    }
}
