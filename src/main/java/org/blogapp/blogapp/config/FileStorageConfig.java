package org.blogapp.blogapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//WebMvcConfigure allows us to customize spring mvc settings
//addResourceHanler to serve static file

@Configuration
public class FileStorageConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry){
        registry.addResourceHandler("uploads/**").    //to upload physical folder  on the server
                addResourceLocations("file:uploads/");
    }
}
