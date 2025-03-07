package org.blogapp.blogapp.service;

import org.blogapp.blogapp.model.Blog;
import org.blogapp.blogapp.repository.BlogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class Blogservice {

    @Autowired
    private BlogRepository blogRepository;

    public List<Blog> getAllBlogs(){
        return blogRepository.findAll();
    }

    public Optional<Blog> getBlogById(long id){
        return blogRepository.findById(id);
    }

    public Blog addBlog(Blog blog){
        blog.setCreated_at(LocalDateTime.now());
        blog.setUpdated_at(LocalDateTime.now());
        return blogRepository.save(blog);
    }

    public Blog updateBlog(long id,Blog blogDetails){
        Blog blog =blogRepository.findById(id).orElseThrow(()->new RuntimeException("Blog not found"));
        blog.setTitle(blogDetails.getTitle());
        blog.setContent(blogDetails.getContent());
        blog.setUpdated_at(LocalDateTime.now());
        return blogRepository.save(blog);
    }
    public Blog deleteBlog(Long id){
        return blogRepository.findById(id).orElseThrow(()->new RuntimeException("Blog not found"));
    }
}

