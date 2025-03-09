package org.blogapp.blogapp.service;

import lombok.RequiredArgsConstructor;
import org.blogapp.blogapp.dto.BlogDTO;
import org.blogapp.blogapp.dto.BlogRequestDTO;
import org.blogapp.blogapp.mappper.BlogMapper;
import org.blogapp.blogapp.model.Blog;
import org.blogapp.blogapp.repository.BlogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.blogapp.blogapp.mappper.BlogMapper;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class BlogService {

    @Autowired
    private final BlogRepository blogRepository;

    private BlogDTO convertToDTO(Blog blog){
        BlogDTO blogDTO = new BlogDTO();
        blogDTO.setId(blog.getId());
        blogDTO.setTitle(blog.getTitle());
        blogDTO.setContent(blog.getContent());
        blogDTO.setCreated_at(LocalDateTime.now());
        blogDTO.setUpdated_at(LocalDateTime.now());
        return blogDTO;
    }
    public List<BlogDTO> getAllBlogs(){
        return blogRepository.findAll().stream().map(BlogMapper.INSTANCE::toDTO).collect(Collectors.toList());
    }

    public BlogDTO getBlogById(int id){

        Optional<Blog> blog = blogRepository.findById(id);
        return blog.map(BlogMapper.INSTANCE::toDTO).orElse(null);
    }

    public BlogDTO createBlog(BlogRequestDTO blogRequestDTO) {
        Blog blog = new Blog();
        blog.setTitle(blogRequestDTO.getTitle());
        blog.setContent(blogRequestDTO.getContent());
        blog = blogRepository.save(blog);
        return convertToDTO(blog);
    }
    // Update a blog from a request DTO
    public BlogDTO updateBlog(int id, BlogRequestDTO blogRequestDTO) {
        Blog blog = blogRepository.findById(id).orElseThrow(() -> new RuntimeException("Blog not found"));
        blog.setTitle(blogRequestDTO.getTitle());
        blog.setContent(blogRequestDTO.getContent());
        blog = blogRepository.save(blog);
        return convertToDTO(blog);
    }

    public String deleteBlog(int id){
        Blog blog=blogRepository.findById(id).orElseThrow(()-> new RuntimeException("Blog not found"));
        blogRepository.delete(blog);
        return "Blog with ID " + id + " has been deleted successfully.";
    }

}

//    public List<Blog> getAllBlogs(){
//        return blogRepository.findAll();
//    }
//
//    public Optional<Blog> getBlogById(long id){       //allow null value
//        return blogRepository.findById(id);
//    }
//
//    public Blog addBlog(Blog blog){
//        blog.setCreated_at(LocalDateTime.now());
//        blog.setUpdated_at(LocalDateTime.now());
//        return blogRepository.save(blog);
//    }
//
//    public Blog updateBlog(long id,Blog blogDetails){
//        Blog blog =blogRepository.findById(id).orElseThrow(()->new RuntimeException("Blog not found"));
//        blog.setTitle(blogDetails.getTitle());
//        blog.setContent(blogDetails.getContent());
//        blog.setUpdated_at(LocalDateTime.now());
//        return blogRepository.save(blog);
//    }
//    public Blog deleteBlog(Long id){
//        return blogRepository.findById(id).orElseThrow(()->new RuntimeException("Blog not found"));
//    }


