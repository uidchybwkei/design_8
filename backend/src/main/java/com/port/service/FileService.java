package com.port.service;

import com.port.entity.FileAttachment;
import com.port.exception.BusinessException;
import com.port.mapper.FileAttachmentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class FileService {

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    @Value("${file.upload.url-prefix:/uploads}")
    private String urlPrefix;

    @Autowired
    private FileAttachmentMapper fileAttachmentMapper;

    public FileAttachment upload(MultipartFile file, String bizType, Long bizId, Long uploaderId) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String newFileName = UUID.randomUUID().toString().replace("-", "") + extension;
        String relativePath = datePath + "/" + newFileName;
        
        File uploadDir = new File(uploadPath);
        if (!uploadDir.isAbsolute()) {
            uploadDir = new File(System.getProperty("user.dir"), uploadPath);
        }
        
        File destFile = new File(uploadDir, relativePath);
        File parentDir = destFile.getParentFile();
        
        if (!parentDir.exists()) {
            boolean created = parentDir.mkdirs();
            if (!created && !parentDir.exists()) {
                throw new BusinessException("创建上传目录失败: " + parentDir.getAbsolutePath());
            }
        }

        try {
            file.transferTo(destFile);
        } catch (IOException e) {
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }

        FileAttachment attachment = new FileAttachment();
        attachment.setFileName(originalFilename);
        attachment.setFilePath(relativePath);
        attachment.setFileUrl(urlPrefix + "/" + relativePath);
        attachment.setFileSize(file.getSize());
        attachment.setFileType(file.getContentType());
        attachment.setBizType(bizType);
        attachment.setBizId(bizId);
        attachment.setUploaderId(uploaderId);
        attachment.setCreateTime(LocalDateTime.now());

        fileAttachmentMapper.insert(attachment);

        return attachment;
    }
}
