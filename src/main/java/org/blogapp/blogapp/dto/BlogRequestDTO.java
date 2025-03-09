package org.blogapp.blogapp.dto;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class BlogRequestDTO {
    private String title;
    private String content;
}
//    public void setTitle(String title) {
//        this.title = title;
//    }
//    public String getTitle() {
//        return title;
//    }
//    public void setContent(String content) {
//        this.content = content;
//    }
//    public String getContent() {
//        return content;
//    }
//}
