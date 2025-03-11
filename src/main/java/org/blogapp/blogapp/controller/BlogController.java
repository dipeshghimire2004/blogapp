package org.blogapp.blogapp.controller;


import org.blogapp.blogapp.dto.BlogDTO;
import org.blogapp.blogapp.dto.BlogRequestDTO;
import org.blogapp.blogapp.service.BlogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("api/blog")
public class BlogController {

    @Autowired
    private BlogService blogService;


    @PostMapping(consumes="multipart/form-data")
    public ResponseEntity<BlogDTO> createBlog(@RequestParam("title") String title, @RequestParam("photo") MultipartFile photo, @RequestParam("content") String content) throws Exception {
        BlogRequestDTO blogRequestDTO = new BlogRequestDTO();
        blogRequestDTO.setTitle(title);
        blogRequestDTO.setPhoto(photo);
        blogRequestDTO.setContent(content);
//        if(blogRequestDTO.getPhoto() != null && blogRequestDTO.isphotoSizeValid()){
//            throw new IllegalArgumentException("file size must be less than 5MB");
//        }

        BlogDTO addBlog= blogService.createBlog(blogRequestDTO);
        return new ResponseEntity<>(addBlog, HttpStatus.CREATED);
    }

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


    @PutMapping("/{id}")
    public BlogDTO updateBlog(@PathVariable int id, @RequestParam("title") String title, @RequestParam("photo") MultipartFile photo, @RequestParam("content") String Content) throws Exception {
        BlogRequestDTO blogRequestDTO = new BlogRequestDTO();
        blogRequestDTO.setTitle(title);
        blogRequestDTO.setPhoto(photo);
        blogRequestDTO.setContent(Content);
//        BlogDTO updatedBlog=blogService.updateBlog(id, blogRequestDTO);
        return blogService.updateBlog(id, blogRequestDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBlog(@PathVariable int id) {
        blogService.deleteBlog(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

