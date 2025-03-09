package org.blogapp.blogapp.controller;

import org.blogapp.blogapp.dto.BlogDTO;
import org.blogapp.blogapp.dto.BlogRequestDTO;
import org.blogapp.blogapp.service.BlogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/blog")
public class BlogController {

    @Autowired
    private BlogService blogservice;

    @GetMapping
    public List<BlogDTO> getAllBlog(){
        return blogservice.getAllBlogs();
    }

    @GetMapping("/{id}")
    public BlogDTO getBlogById(@PathVariable int id){
        return blogservice.getBlogById(id);
    }

    @PostMapping
    public BlogDTO addBlog(@RequestBody BlogRequestDTO blogRequestDTO){
        return blogservice.createBlog(blogRequestDTO);
    }
    @PutMapping("/{id}")
    public BlogDTO updateBlog(@PathVariable int id, @RequestBody BlogRequestDTO blogRequestDTO) {
        return blogservice.updateBlog(id, blogRequestDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteBlog(@PathVariable int id){
         blogservice.deleteBlog(id);
    }
}

