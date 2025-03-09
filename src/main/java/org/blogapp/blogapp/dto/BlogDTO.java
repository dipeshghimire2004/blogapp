package org.blogapp.blogapp.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.*;

@Data
@NoArgsConstructor
//@AllArgsConstructor
//@Builder
public class BlogDTO {
    private int id;
    private String title;
    private String content;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

//    public int getId() {
//        return id;
//    }
//
//    public void setId(int id) {
//        this.id = id;
//    }
//    public String getTitle() {
//        return title;
//    }
//    public void setTitle(String title) {
//        this.title = title;
//    }
//    public String getContent() {
//        return content;
//    }
//    public void setContent(String content) {
//        this.content = content;
//    }
//    public LocalDateTime getCreated_at() {
//        return created_at;
//    }
//    public void setCreated_at(LocalDateTime created_at) {
//        this.created_at = created_at;
//    }
//    public LocalDateTime getUpdated_at() {
//        return updated_at;
//    }
//    public void setUpdated_at(LocalDateTime updated_at) {
//        this.updated_at = updated_at;
//    }
}
