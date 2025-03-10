package org.blogapp.blogapp.controller;

import org.blogapp.blogapp.dto.BlogDTO;
import org.blogapp.blogapp.dto.BlogRequestDTO;
import org.blogapp.blogapp.service.BlogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/blog")
public class BlogController {

    @Autowired
    private BlogService blogService;

    @GetMapping
    public List<BlogDTO> getAllBlog(){
        List <BlogDTO> blogs = blogService.getAllBlogs();
        return new ResponseEntity<>(blogs, HttpStatus.OK).getBody();

    }

    @GetMapping("/{id}")
    public BlogDTO getBlogById(@PathVariable int id){
        BlogDTO blog = blogService.getBlogById(id);
        return new ResponseEntity<>(blog, HttpStatus.OK).getBody();
    }

    @PostMapping
    public BlogDTO addBlog(@RequestBody BlogRequestDTO blogRequestDTO){
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location","/api/data/123");
        BlogDTO createdBlog = blogService.createBlog(blogRequestDTO);
        return new ResponseEntity<>(createdBlog, HttpStatus.OK).getBody();
//        return blogService.createBlog(blogRequestDTO);
    }
    @PutMapping("/{id}")
    public BlogDTO updateBlog(@PathVariable int id, @RequestBody BlogRequestDTO blogRequestDTO) {
        BlogDTO updatedBlog=blogService.updateBlog(id, blogRequestDTO);
        return new ResponseEntity<>(updatedBlog, HttpStatus.OK).getBody();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBlog(@PathVariable int id) {
        blogService.deleteBlog(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

