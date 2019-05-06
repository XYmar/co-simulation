package com.rengu.cosimulation.controller;

import com.rengu.cosimulation.entity.*;
import com.rengu.cosimulation.service.PreviewFileService;
import com.rengu.cosimulation.service.SublibraryFilesService;
import com.rengu.cosimulation.service.SubtaskFilesService;
import com.rengu.cosimulation.utils.FileUtil;
import com.rengu.cosimulation.utils.PreviewFileInit;
import com.rengu.cosimulation.utils.ResultUtils;
import com.rengu.cosimulation.utils.SHAUtil;
import lombok.Cleanup;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author YJH
 * @Date 2019/3/13 10:08
 */
@RestController
@RequestMapping(value = "/preview")
public class PreviewFileController {

    private final PreviewFileService previewFileService;
    private final SubtaskFilesService subtaskFilesService;
    private final SublibraryFilesService sublibraryFilesService;
    private PreviewFileInit previewFileInit;

    @Autowired
    public PreviewFileController(PreviewFileInit previewFileInit, PreviewFileService previewFileService, SubtaskFilesService subtaskFilesService, SublibraryFilesService sublibraryFilesService) {
        this.previewFileInit = previewFileInit;
        this.previewFileService = previewFileService;
        this.subtaskFilesService = subtaskFilesService;
        this.sublibraryFilesService = sublibraryFilesService;
    }

    @Value("${tmp.root}")
    private String rootPath;

    @Value("${text.type}")
    private String textType;

    @Value("${img.type}")
    private String imgType;

    @Value("${office.type}")
    private String officeType;

    @Value("${compress.type}")
    private String compressType;

    @Value("${pdf.type}")
    private String pdfType;

    @GetMapping("/previewSublibraryFile")
    public ResultEntity localSublibraryFile(String sublibraryFileId) {
        SublibraryFilesEntity sublibraryFilesEntity = sublibraryFilesService.getSublibraryFileById(sublibraryFileId);
        String filePath = sublibraryFilesEntity.getFileEntity().getLocalPath();
        return localAllFiles(filePath);
    }

    private ResultEntity localAllFiles(String filePath) {
        PreviewFileEntity previewFileEntity = new PreviewFileEntity();
        previewFileEntity.setFilePath(filePath);
        previewFileEntity.setFileId(SHAUtil.SHAHashCode(filePath));
        // 判断文件是否存在
        //  拿到文件地址转换文件
        previewFileInit.saveFile(previewFileEntity);
        previewFileService.conventer(previewFileEntity);
//        return previewUrl(previewFileEntity, model, request);
        System.out.print(previewUrl(previewFileEntity).size());
        System.out.print(previewUrl(previewFileEntity).get("fileType"));
        return ResultUtils.success(previewUrl(previewFileEntity));
    }

    @GetMapping("/previewFile")
    public ResultEntity localFile(String subtaskFileId) {
        SubtaskFilesEntity subtaskfileEntity = subtaskFilesService.getSubtaskFileById(subtaskFileId);
        String filePath = subtaskfileEntity.getFileEntity().getLocalPath();
        return localAllFiles(filePath);
    }

    /**
     * 获取重定向路径
     */
    private Map<String, String> previewUrl(PreviewFileEntity PreviewFileEntity) {
        File file = new File(rootPath + File.separator + PreviewFileEntity.getFileId()
                + File.separator + "resource" + File.separator + PreviewFileEntity.getConventedFileName());
        String subfix = FileUtil.getFileSufix(PreviewFileEntity.getFilePath());
        //返回一个后缀名以及pathId给你，根据后缀名判断该通往哪个页面
        Map<String, String> map = new HashMap<>();
        map.put("pathId", PreviewFileEntity.getFileId());
        map.put("fileSubfixType", subfix);
        if (file.exists()) {
            // 判断文件类型，不同的文件做不同的展示
            if (this.pdfType.contains(subfix.toLowerCase())) {
                map.put("fileType", "office");
            } else if (this.textType.contains(subfix.toLowerCase())) {
                map.put("fileType", "txt");
            } else if (this.imgType.contains(subfix.toLowerCase())) {
                map.put("fileType", "picture");
            } else if (this.compressType.contains(subfix.toLowerCase())) {
                map.put("fileTree", PreviewFileEntity.getFileTree());
            } else if (this.officeType.contains(subfix.toLowerCase())) {
                if ("pptx".equalsIgnoreCase(subfix.toLowerCase()) ||
                        "ppt".equalsIgnoreCase(subfix.toLowerCase())) {
                    List<String> imgFiles = previewFileInit.getImageFilesOfPPT(PreviewFileEntity.getFileId());
                    StringBuilder imgPaths = new StringBuilder();
                    for (String s : imgFiles) {
                        imgPaths.append(PreviewFileEntity.getFileId()).append("/resource/").append(s.substring(s.lastIndexOf("\\"))).append(",");
                    }
                    map.put("imgPaths", imgPaths.toString());
                    map.put("fileType", "ppt");
                } else {
                    map.put("fileType", "office");
                }
            }
        } else {
            map.put("fileType", "fileNotSupported");
        }
        return map;
//        if (file.exists()) {
//            // 判断文件类型，不同的文件做不同的展示
//            if (this.pdfType.contains(subfix.toLowerCase())) {
//                return "office";
//            } else if (this.textType.contains(subfix.toLowerCase())) {
//                return "txt";
//            } else if (this.imgType.contains(subfix.toLowerCase())) {
//                return "picture";
//            } else if (this.compressType.contains(subfix.toLowerCase())) {
//                model.addAttribute("fileTree", PreviewFileEntity.getFileTree());
//                return "compress";
//            } else if (this.officeType.contains(subfix.toLowerCase())) {
//                if ("pptx".equalsIgnoreCase(subfix.toLowerCase()) ||
//                        "ppt".equalsIgnoreCase(subfix.toLowerCase())) {
//                    List<String> imgFiles = previewFileInit.getImageFilesOfPPT(PreviewFileEntity.getFileId());
//                    String imgPaths = "";
//                    for (String s : imgFiles) {
//                        imgPaths += (PreviewFileEntity.getFileId() + "/resource/"
//                                + s.substring(s.lastIndexOf("\\"), s.length()) + ",");
//                    }
//                    model.addAttribute("imgPaths", imgPaths);
//                    return "ppt";
//                } else {
//                    return "office";
//                }
//            }
//        } else {
//            return "forward:/fileNotSupported";
//        }
//        return null;
    }

//     * 获取预览文件

    @GetMapping(value = "/viewer/document/{pathId}")
    public void onlinePreview(@PathVariable String pathId, String fileFullPath, HttpServletResponse response) throws IOException {
        PreviewFileEntity previewFileEntity = previewFileInit.findByHashCode(pathId);
        // 得到转换后的文件地址
        String fileUrl;
        if (fileFullPath != null) {
            fileUrl = rootPath + File.separator + fileFullPath;
        } else {
            if (previewFileEntity.getConventedFileName() == null || previewFileEntity.getConventedFileName().equals("")) {
                fileUrl = rootPath + File.separator + previewFileEntity.getFileId() + File.separator + "resource" + File.separator;
            }
            fileUrl = rootPath + File.separator + previewFileEntity.getFileId() + File.separator + "resource" + File.separator + previewFileEntity.getConventedFileName();
        }
        File file = new File(fileUrl);
        // 设置内容长度
        response.setContentLength((int) file.length());
        // 内容配置中要转码,inline 浏览器支持的格式会在浏览器中打开,否则下载
        String fullFileName = FileUtil.getFileName(previewFileEntity.getFilePath());
        response.setHeader("Content-Disposition", "inline;fileName=\"" + fullFileName + "\"");
        // 设置content-type
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.setContentType(previewFileEntity.getOriginalMIMEType());
        System.out.println(previewFileEntity.getOriginalMIMEType());
        @Cleanup FileInputStream is = new FileInputStream(new File(fileUrl));
        @Cleanup OutputStream os = response.getOutputStream();
        IOUtils.copy(is, os);
        response.flushBuffer();
//        FileInputStream is = new FileInputStream(new File(fileUrl));
//        OutputStream os = response.getOutputStream();
//        printFile(is, os);
    }

//    private void printFile(FileInputStream is, OutputStream os) throws IOException {
//        try {
//            byte[] bytes = new byte[1024];
//            int tmp = 0;
//            while ((tmp = is.read(bytes)) != -1) {
//                os.write(bytes, 0, tmp);
//                os.flush();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (os != null) {
//                os.close();
//            }
//            if (is != null) {
//                is.close();
//            }
//        }
//    }
}
