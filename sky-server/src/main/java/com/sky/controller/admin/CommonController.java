package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

//通用接口
@RestController
@RequestMapping("/admin/common")
@Slf4j
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;

    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file)  {
        log.info("文件上传:{}",file);
        try {
            String originalFileName = file.getOriginalFilename();
            //截取原始文件的后缀，如*.png
            String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            String name = UUID.randomUUID().toString() + extension; //使用
            String filePath = aliOssUtil.upload(file.getBytes(), name); //上传成功后会返回文件路径
            return Result.success(filePath);
        }catch (IOException e){
           log.error("文件上传失败:{}",e);
        }

        return Result.error("上传失败");
    }
}
