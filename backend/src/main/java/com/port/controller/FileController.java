package com.port.controller;

import com.port.common.Result;
import com.port.entity.FileAttachment;
import com.port.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private FileService fileService;

    @PostMapping("/upload")
    public Result<FileAttachment> upload(@RequestParam("file") MultipartFile file,
                                         @RequestParam(required = false) String bizType,
                                         @RequestParam(required = false) Long bizId,
                                         @RequestAttribute("userId") Long userId) {
        FileAttachment attachment = fileService.upload(file, bizType, bizId, userId);
        return Result.success(attachment);
    }
}
