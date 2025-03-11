package org.blogapp.blogapp.dto;

import java.time.LocalDateTime;
import lombok.*;

@Data
@NoArgsConstructor
public class BlogDTO {
    private int id;
    private String title;
    private String photoUrl;
    private String content;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    //custom constructor
//    public BlogDTO(){
//        this.photoUrl="http://localhost:8080/api/blog/default.jpg";
//    }
    public String getPhotoUrl(){
        return (photoUrl ==null || photoUrl.isEmpty())?"http://localhost:8080/api/blog/default.jpg":photoUrl;
    }
}
