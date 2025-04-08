package org.blogapp.dg_blogapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;


/**
 * Data Transfer Object for returning blog post details to the client.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BlogPostResponseDTO {
    private int id;
    private String title;
    private String content;
    private String imageUrl;
    private String authorUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
