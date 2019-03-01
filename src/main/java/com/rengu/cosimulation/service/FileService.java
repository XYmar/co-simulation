package com.rengu.cosimulation.service;

import com.rengu.cosimulation.entity.ChunkEntity;
import com.rengu.cosimulation.entity.FileEntity;
import com.rengu.cosimulation.enums.ResultCode;
import com.rengu.cosimulation.exception.ResultException;
import com.rengu.cosimulation.repository.FileRepository;
import com.rengu.cosimulation.utils.ApplicationConfig;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Author: XYmar
 * Date: 2019/2/28 11:14
 */
@Slf4j
@Service
@Transactional
public class FileService {
    @Autowired
    private FileRepository fileRepository;

    // 根据Md5判断文件是否存在
    public boolean hasFileByMD5(String MD5) {
        if (StringUtils.isEmpty(MD5)) {
            return false;
        }
        return fileRepository.existsByMD5(MD5);
    }

    // 保存文件块
    public void saveChunk(ChunkEntity chunkEntity, MultipartFile multipartFile) throws IOException {
        File chunk = new File(ApplicationConfig.CHUNKS_SAVE_PATH + File.separator + chunkEntity.getIdentifier() + File.separator + chunkEntity.getChunkNumber() + ".tmp");
        chunk.getParentFile().mkdirs();
        chunk.createNewFile();
        IOUtils.copy(multipartFile.getInputStream(), new FileOutputStream(chunk));
    }

    // 合并文件块
    public synchronized FileEntity mergeChunks(ChunkEntity chunkEntity, FileEntity fileEntity) throws IOException {
        if (hasFileByMD5(chunkEntity.getIdentifier())) {
            return getFileByMD5(chunkEntity.getIdentifier());
        } else {
            File file = null;
            String extension = FilenameUtils.getExtension(chunkEntity.getFilename());
            if (StringUtils.isEmpty(extension)) {
                file = new File(ApplicationConfig.FILES_SAVE_PATH + File.separator + fileEntity.getType() + File.separator + chunkEntity.getIdentifier());
            } else {
                file = new File(ApplicationConfig.FILES_SAVE_PATH + File.separator + fileEntity.getType() + File.separator + chunkEntity.getIdentifier() + "." + FilenameUtils.getExtension(chunkEntity.getFilename()));
            }
            return mergeChunks(file, chunkEntity, fileEntity);
        }
    }

    // 保存文件信息
    @CacheEvict(value = "File_Cache", allEntries = true)
    public FileEntity saveFile(File file, FileEntity fileEntityArgs) throws IOException {
        FileEntity fileEntity = new FileEntity();
        @Cleanup FileInputStream fileInputStream = new FileInputStream(file);
        String MD5 = DigestUtils.md5Hex(fileInputStream);
        if (hasFileByMD5(MD5)) {
            throw new ResultException(ResultCode.FILE_MD5_EXISTED_ERROR);
        }
        fileEntity.setMD5(MD5);                                                           // MD5
        fileEntity.setPostfix(FilenameUtils.getExtension(file.getName()));                // 后缀
        fileEntity.setFileSize(FileUtils.sizeOf(file));                                   // 大小
        fileEntity.setType(fileEntityArgs.getType());                                     // 文件类型
        fileEntity.setSecretClass(fileEntityArgs.getSecretClass());                       // 文件密级
        fileEntity.setLocalPath(file.getAbsolutePath());                                  // 路径
        return fileRepository.save(fileEntity);
    }

    // 根据Id删除文件
    @CacheEvict(value = "File_Cache", allEntries = true)
    public FileEntity deleteFileById(String fileId) throws IOException {
        FileEntity fileEntity = getFileById(fileId);
        FileUtils.forceDeleteOnExit(new File(fileEntity.getLocalPath()));
        fileRepository.delete(fileEntity);
        return fileEntity;
    }

    // 检查文件块是否存在
    public boolean hasChunk(ChunkEntity chunkEntity) {
        File chunk = new File(ApplicationConfig.CHUNKS_SAVE_PATH + File.separator + chunkEntity.getIdentifier() + File.separator + chunkEntity.getChunkNumber() + ".tmp");
        return chunk.exists() && chunkEntity.getChunkSize() == FileUtils.sizeOf(chunk);
    }

    // 根据Id判断文件是否存在
    public boolean hasFileById(String fileId) {
        if (StringUtils.isEmpty(fileId)) {
            return false;
        }
        return fileRepository.existsById(fileId);
    }

    // 根据Id查询文件
    @Cacheable(value = "File_Cache", key = "#fileId")
    public FileEntity getFileById(String fileId) {
        if (!hasFileById(fileId)) {
            throw new ResultException(ResultCode.FILE_ID_NOT_FOUND_ERROR);
        }
        return fileRepository.findById(fileId).get();
    }

    // 根据MD5查询文件
    @Cacheable(value = "File_Cache", key = "#MD5")
    public FileEntity getFileByMD5(String MD5) {
        if (!hasFileByMD5(MD5)) {
            throw new ResultException(ResultCode.FILE_MD5_NOT_FOUND_ERROR);
        }
        return fileRepository.findByMD5(MD5).get();
    }

    // 查询所有文件
    public Page<FileEntity> getFiles(Pageable pageable) {
        return fileRepository.findAll(pageable);
    }



    private FileEntity mergeChunks(File file, ChunkEntity chunkEntity, FileEntity fileEntity) throws IOException {
        file.delete();
        file.getParentFile().mkdirs();
        file.createNewFile();
        for (int i = 1; i <= chunkEntity.getTotalChunks(); i++) {
            File chunk = new File(ApplicationConfig.CHUNKS_SAVE_PATH + File.separator + chunkEntity.getIdentifier() + File.separator + i + ".tmp");
            if (chunk.exists()) {
                FileUtils.writeByteArrayToFile(file, FileUtils.readFileToByteArray(chunk), true);
            } else {
                throw new ResultException(ResultCode.FILE_CHUNK_NOT_FOUND_ERROR);
            }
        }
        @Cleanup FileInputStream fileInputStream = new FileInputStream(file);
        if (!chunkEntity.getIdentifier().equals(DigestUtils.md5Hex(fileInputStream))) {
            throw new RuntimeException("文件合并失败，请检查：" + file.getAbsolutePath() + "是否正确。");
        }
        return saveFile(file, fileEntity);
    }
}
