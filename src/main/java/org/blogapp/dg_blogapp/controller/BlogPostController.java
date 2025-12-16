package org.blogapp.dg_blogapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.blogapp.dg_blogapp.dto.BlogPostFilterRequest;
import org.blogapp.dg_blogapp.dto.BlogPostRequestDTO;
import org.blogapp.dg_blogapp.dto.BlogPostResponseDTO;
import org.blogapp.dg_blogapp.dto.PageResponse;
import org.blogapp.dg_blogapp.service.BlogPostService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/blog")
@Validated
@RequiredArgsConstructor
@Slf4j
public class BlogPostController {

    private final BlogPostService blogPostService;

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
    @PostMapping("/create")
    public ResponseEntity<BlogPostResponseDTO> createPost(
            @Valid @ModelAttribute BlogPostRequestDTO requestDTO,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        log.info("Received request to create post with title: {}", requestDTO.getTitle());
        BlogPostResponseDTO responseDTO = blogPostService.createPost(image,requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @Operation(summary = "Update a blog post", description = "Updates an existing blog post (author or admin only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Post updated"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Post not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<BlogPostResponseDTO> updatePost(@PathVariable UUID id,
                                                          @Valid @RequestBody BlogPostRequestDTO postRequestDTO,
                                                          @RequestPart(value="image", required = false) MultipartFile image) {
        log.info("Received request to update a blog post with id {}",id);
        BlogPostResponseDTO updatePost= blogPostService.updatePost(id, postRequestDTO,image);
        return ResponseEntity.ok(updatePost);
    }

    @GetMapping("/pageform")
    public ResponseEntity<PageResponse<BlogPostResponseDTO>> getPosts(@RequestParam(defaultValue="0") int pageNo, @RequestParam(defaultValue="10") int pageSize,
                                                                      @RequestParam(defaultValue = "createdAt") String sortBy,
                                                                      @RequestParam(defaultValue = "aesc") String sortOrder) {
        log.info("Fetching 10 posts");
        PageResponse<BlogPostResponseDTO> posts = blogPostService.getPosts(pageNo, pageSize, sortBy, sortOrder);
        return ResponseEntity.status(HttpStatus.OK).body(posts);
    }

    @GetMapping("/title/{title}")
    public ResponseEntity<PageResponse<BlogPostResponseDTO>> getPost(@PathVariable String title,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "10") int size) {
        log.info("Fetching  posts by title name {}", title);
        PageResponse<BlogPostResponseDTO> posts= blogPostService.getPostsByTitle(title, page, size);
        return ResponseEntity.status(HttpStatus.OK).body(posts);
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
        log.info("Received request to retrieve all posts");
        List<BlogPostResponseDTO> posts = blogPostService.getAllPosts();
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/search")
    public ResponseEntity<List<BlogPostResponseDTO>> getAllPosts(@ModelAttribute BlogPostFilterRequest filter) {
        log.info("Received request to retrieve all posts");
        List<BlogPostResponseDTO> posts = blogPostService.getPostsForSearch(filter);
        return ResponseEntity.ok(posts);
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
    public ResponseEntity<BlogPostResponseDTO> getPostById(@PathVariable UUID id){
        log.info("Received request to retrieve a blog post with id {}",id);
        BlogPostResponseDTO post= blogPostService.getPostById(id);
        return ResponseEntity.ok(post);
    }





    @Operation(summary = "Soft delete a blog post", description = "Marks a blog post as deleted (author or admin only)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Post deleted"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Post not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<BlogPostResponseDTO> deletePost(@PathVariable UUID id){
        log.info("Received request to delete a blog post with id {}",id);
        blogPostService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}
