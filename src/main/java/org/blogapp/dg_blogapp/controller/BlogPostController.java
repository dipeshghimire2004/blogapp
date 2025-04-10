package org.blogapp.dg_blogapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(summary = "Get all blog posts", description = "Retrieves a list of all non-deleted blog posts")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
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
    @Operation(summary = "Create a new blog post", description = "Creates a blog post with optional image upload")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Post created"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BlogPostResponseDTO> createPost(
            @Valid @ModelAttribute BlogPostRequestDTO requestDTO,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        logger.info("Received request to create post with title: {}", requestDTO.getTitle());
        BlogPostResponseDTO responseDTO = blogPostService.createPost(requestDTO, image);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }
    /**
     * Retrieves a blog post by its ID.
     *
     * @param id the ID of the blog post
     * @return the blog post
     */
    @Operation(summary = "Get a blog post by ID", description = "Fetches a specific blog post by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Post found"),
            @ApiResponse(responseCode = "404", description = "Post not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BlogPostResponseDTO> getPostById(@PathVariable int id){
        logger.info("Received request to retrieve a blog post with id {}",id);
        BlogPostResponseDTO post= blogPostService.getPostById(id);
        return ResponseEntity.ok(post);
    }


    @Operation(summary = "Update a blog post", description = "Updates an existing blog post (author or admin only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Post updated"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Post not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<BlogPostResponseDTO> updatePost(@PathVariable int id,@Valid @RequestBody BlogPostRequestDTO postRequestDTO){
        logger.info("Received request to update a blog post with id {}",id);
        BlogPostResponseDTO updatePost= blogPostService.updatePost(id, postRequestDTO);
        return ResponseEntity.ok(updatePost);
    }


    @Operation(summary = "Soft delete a blog post", description = "Marks a blog post as deleted (author or admin only)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Post deleted"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Post not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<BlogPostResponseDTO> deletePost(@PathVariable int id){
        logger.info("Received request to delete a blog post with id {}",id);
        blogPostService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}
