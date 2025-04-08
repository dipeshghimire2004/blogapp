package org.blogapp.dg_blogapp.mapper;

import org.blogapp.dg_blogapp.dto.BlogPostResponseDTO;
import org.blogapp.dg_blogapp.model.BlogPost;
import org.springframework.stereotype.Component;

/**
 * Mapper class to convert between Blogpost entity and DTOs
 */

@Component  //Made it a Spring @Component for dependency injection instead of static methods.
public class BlogPostMapper {
    /**
     * Converts a BlogPost entity into a BlogPostResponseDTO
     * @param post the  BlogPost to convert
     * @return the corresponding BlogPostResponseDTO
     */
    public BlogPostResponseDTO toDto (BlogPost post) {
        if (post == null) {
            return null;
        }
        return BlogPostResponseDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .imageUrl(post.getImageUrl())
                .authorUsername(post.getUser().getUsername())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
