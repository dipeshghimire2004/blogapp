package org.blogapp.dg_blogapp.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@Component
public class FileNameGenerator {

    public String generateProductImageName(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if(fileName==null || fileName.isEmpty()){
            throw new RuntimeException("Invalid file name");
        }
        String extension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
        return "certificate_"+ UUID.randomUUID()+extension;
    }

}
