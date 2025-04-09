package org.blogapp.dg_blogapp.mapper;

import org.blogapp.dg_blogapp.dto.BlogPostRequestDTO;
import org.blogapp.dg_blogapp.dto.BlogPostResponseDTO;
import org.blogapp.dg_blogapp.model.BlogPost;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper class to convert between BlogPost entity and DTOs
 */
@Mapper(componentModel = "spring")
public interface BlogPostMapper {

    /**
     * Converts a BlogPost entity into a BlogPostResponseDTO
     * @param blogPost the BlogPost to convert
     * @return the corresponding BlogPostResponseDTO
     */
    @Mapping(target = "authorUsername", source = "user.username")
    BlogPostResponseDTO toDto(BlogPost blogPost);

    /**
     * Converts a BlogPostRequestDTO into a BlogPost entity.
     * Ignores user, id, createdAt, and updatedAt (they will be set manually).
     * @param blogPostRequestDTO the request data
     * @return BlogPost entity
     */
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    BlogPost toEntity(BlogPostRequestDTO blogPostRequestDTO);
}


//    public BlogPostResponseDTO toDto (BlogPost post) {
//        if (post == null) {
//            return null;
//        }
//        return BlogPostResponseDTO.builder()
//                .id(post.getId())
//                .title(post.getTitle())
//                .content(post.getContent())
//                .imageUrl(post.getImageUrl())
//                .authorUsername(post.getUser().getUsername())
//                .createdAt(post.getCreatedAt())
//                .updatedAt(post.getUpdatedAt())
//                .build();
//    }
