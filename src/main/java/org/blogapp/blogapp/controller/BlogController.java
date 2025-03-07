package org.blogapp.blogapp.controller;

import org.blogapp.blogapp.model.Blog;
import org.blogapp.blogapp.service.Blogservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/blog")
public class BlogController {

    @Autowired
    private Blogservice blogservice;

    @GetMapping
    public List<Blog> getAllBlog(){
        return blogservice.getAllBlogs();
    }

    @GetMapping("/{id}")
    public Blog getBlogById(@PathVariable long id){
        return blogservice.getBlogById(id).orElseThrow(()->new RuntimeException("Blog not found"));
    }

    @PostMapping
    public Blog addBlog(@RequestBody Blog blog){
        return blogservice.addBlog(blog);
    }

    @PutMapping("/{id}")
    public Blog updateBlog(@PathVariable long id, @RequestBody Blog blog){
        return blogservice.updateBlog(id, blog);
    }
    @DeleteMapping("/{id}")
    public Blog deleteBlog(@PathVariable long id){
        return blogservice.deleteBlog(id);
    }
}

