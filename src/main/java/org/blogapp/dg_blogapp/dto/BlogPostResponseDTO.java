package org.blogapp.dg_blogapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;


/**
 * Data Transfer Object for returning blog post details to the client.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BlogPostResponseDTO {
    private UUID id;
    private String title;
    private String content;
    private String imageUrl;
    private String authorUsername;

    private boolean featured=false;

    private String tags;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
