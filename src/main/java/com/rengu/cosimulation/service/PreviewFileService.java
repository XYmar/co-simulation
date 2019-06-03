package com.rengu.cosimulation.service;

import com.rengu.cosimulation.conventer.*;
import com.rengu.cosimulation.entity.PreviewFile;
import com.rengu.cosimulation.utils.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @Author YJH
 * @Date 2019/3/18 15:08
 */
@Component
public class PreviewFileService {
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

    @Autowired
    private CompressedFileConventer compressedFileConventer;

    @Autowired
    private ImageFileConventer imageFileConventer;

    @Autowired
    private OfficeFileConventer officeFileConventer;

    @Autowired
    private PdfFileConventer pdfFileConventer;

    @Autowired
    private TextFileConventer textFileConventer;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    public void conventer(PreviewFile previewFile) {
//        if (PreviewFile.getState() != PreviewFile.STATE_YXZ) {
//            throw new RuntimeException("the files state:" + PreviewFile.getState()
//                    + " is not 2.");
//        }
        try {
            //  获得文件名的后缀
            String subfix = FileUtil.getFileSufix(previewFile.getFilePath());
            if(this.pdfType.contains(subfix.toLowerCase())) {
                this.pdfFileConventer.conventer(previewFile);
            }else if(this.textType.contains(subfix.toLowerCase())) {
                this.textFileConventer.conventer(previewFile);
            }else if(this.imgType.contains(subfix.toLowerCase())) {
                this.imageFileConventer.conventer(previewFile);
            }else if(this.compressType.contains(subfix.toLowerCase())) {
                this.compressedFileConventer.conventer(previewFile);
            }else if(this.officeType.contains(subfix.toLowerCase())) {
             /*   if("xlsx".equals(subfix.toLowerCase()) || "xls".equals(subfix.toLowerCase())
                        || "pptx".equals(subfix.toLowerCase()) || "ppt".equals(subfix.toLowerCase())) {
                    this.officeFileConventer.conventerToHtml(previewFile);
                }else {*/
                    this.officeFileConventer.conventerToPdf(previewFile);
//                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            logger.error("不支持该类型文件的转换");
            throw new RuntimeException(e);
        }
    }

}
