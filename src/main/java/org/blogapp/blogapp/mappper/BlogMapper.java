package org.blogapp.blogapp.mappper;

import org.blogapp.blogapp.dto.BlogDTO;
import org.blogapp.blogapp.model.Blog;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper     //marks the interface as a MAPstruct mapper
public interface BlogMapper {
    //provide a singleton instance of the wrapper
    BlogMapper INSTANCE= Mappers.getMapper(BlogMapper.class);

    BlogDTO toDTO(Blog blog);
    Blog toEntity(BlogDTO blogDTO);

}
