package org.blogapp.dg_blogapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer for creating or updating a blogpost
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogPostRequestDTO {
    @NotBlank(message = "Title cannot be blank")
    @Size(max = 100, message = "Title must not exceed 100 characters")
    private String title;

    @NotBlank(message="Content cannot be blank")
    private String content;

    @Size(max = 255, message = "Image URL must not exceed 255 characters")
    private String imageUrl;
}
