package org.blogapp.blogapp.dto;
import lombok.*;
import org.antlr.v4.runtime.misc.NotNull;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlogRequestDTO {
    private String title;

//    @NotNull(message = "Photo is required")
    private MultipartFile photo;
    private String content;

    //add custo validation for size is needed
    public boolean isphotoSizeValid(){
        return photo != null && photo.getSize()<= 5*1024*1024;
    }
}
