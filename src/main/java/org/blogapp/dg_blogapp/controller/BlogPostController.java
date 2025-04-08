package org.blogapp.dg_blogapp.controller;

import jakarta.validation.Valid;
import org.blogapp.dg_blogapp.dto.BlogPostRequestDTO;
import org.blogapp.dg_blogapp.dto.BlogPostResponseDTO;
import org.blogapp.dg_blogapp.service.BlogPostService;
import org.blogapp.dg_blogapp.service.S3Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/blog")
@Validated
public class BlogPostController {
//    private static final Logger logger = LoggerFactory.getLoggeer(BlogPostController.class);
   private static final Logger logger = LoggerFactory.getLogger(BlogPostController.class);

    private BlogPostService blogPostService;
    private S3Service s3Service;

    public BlogPostController(BlogPostService blogPostService, S3Service s3Service) {
        this.blogPostService = blogPostService;
        this.s3Service = s3Service;
    }


    /**
     * Retrieves all blog posts.
     *
     * @return a list of blog posts
     */
    @GetMapping()
    public ResponseEntity<List<BlogPostResponseDTO>> getAllPosts() {
        logger.info("Received request to retrieve all posts");
        List<BlogPostResponseDTO> posts = blogPostService.getAllPosts();
        return ResponseEntity.ok(posts);
    }

    /**
     * Creates a new blog post with an optional image.
     *
     * @param requestDTO the blog post data
     * @param image      the optional image file
     * @return the created blog post
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BlogPostResponseDTO> createPost(
            @Valid @ModelAttribute BlogPostRequestDTO requestDTO,
            @RequestPart(value = "file", required = false) MultipartFile image) {
        logger.info("Received request to create post with title: {}", requestDTO.getTitle());
        String imageUrl=null;
        if (image != null && !image.isEmpty()) {
            try {
                imageUrl = s3Service.uploadFile(image);
            } catch (Exception e) {
                logger.error("Failed to upload image for post: {}", requestDTO.getTitle(), e);
                // Continue without image if upload fails
            }
        }
        BlogPostResponseDTO responseDTO = blogPostService.createPost(requestDTO, imageUrl);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }
    /**
     * Retrieves a blog post by its ID.
     *
     * @param id the ID of the blog post
     * @return the blog post
     */
    @GetMapping("/{id}")
    public ResponseEntity<BlogPostResponseDTO> getPostById(@PathVariable int id){
        logger.info("Received request to retrieve a blog post with id {}",id);
        BlogPostResponseDTO post= blogPostService.getPostById(id);
        return ResponseEntity.ok(post);
    }



    @PutMapping("/{id}")
    public ResponseEntity<BlogPostResponseDTO> updatePost(@PathVariable int id,@Valid @RequestBody BlogPostRequestDTO postRequestDTO){
        logger.info("Received request to update a blog post with id {}",id);
        BlogPostResponseDTO updatePost= blogPostService.updatePost(id, postRequestDTO);
        return ResponseEntity.ok(updatePost);
    }
}
