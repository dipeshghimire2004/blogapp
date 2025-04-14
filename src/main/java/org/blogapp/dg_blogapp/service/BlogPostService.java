package org.blogapp.dg_blogapp.service;

import com.amazonaws.services.quicksight.model.UserRole;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.blogapp.dg_blogapp.dto.BlogPostRequestDTO;
import org.blogapp.dg_blogapp.dto.BlogPostResponseDTO;
import org.blogapp.dg_blogapp.exception.BlogPostNotFoundException;
import org.blogapp.dg_blogapp.exception.UnauthorizedException;
import org.blogapp.dg_blogapp.mapper.BlogPostMapper;
import org.blogapp.dg_blogapp.model.BlogPost;
import org.blogapp.dg_blogapp.model.Role;
import org.blogapp.dg_blogapp.model.User;
import org.blogapp.dg_blogapp.repository.PostRepository;
import org.blogapp.dg_blogapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing blog post operation
 */
@Service
@Validated
//@RequiredArgsConstructor
public class BlogPostService extends GenericService<BlogPost, Integer> {
    private static final Logger logger = LoggerFactory.getLogger(BlogPostService.class);

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private BlogPostMapper blogPostMapper;


    @Autowired
    private final S3Service s3Service;

    public BlogPostService(PostRepository postRepository, BlogPostMapper blogPostMapper, S3Service s3Service) {
        super(postRepository);
        this.blogPostMapper = blogPostMapper;
        this.s3Service = s3Service;
    }

    /**
     * retrieves all the blogpost
     * @return a list of blogResponseDTO
     */
    public List<BlogPostResponseDTO> getAllPosts() {
        logger.info("Fetching  all blog posts");
        return findAll().stream()
                .map(blogPostMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * retrieves the blog post by its id
     * @param id the id oft the blogpost
     * @return the blogPostResponseDTO
     */
    // Reuse generic findById and map to DTO
    public BlogPostResponseDTO getPostById(int id) {
        logger.info("Fetching post with ID: {}", id);
        BlogPost post = findById(id)
                .orElseThrow(() -> new BlogPostNotFoundException("Post not found with id: " + id));
        return blogPostMapper.toDto(post);
    }

    /**
     * Creates a new blog post
     * @param postRequestDTO the blog post request data
     * @param image the URL of the uploaded image
     * @return the created BlogPostResponseDTO
     */
    @Transactional
    public BlogPostResponseDTO createPost(@Valid BlogPostRequestDTO postRequestDTO, MultipartFile image) {
        logger.info("Creating new blog post with title: {}", postRequestDTO.getTitle());
        User currentUser= getCurrentUser();
        String imageUrl= image!=null && !image.isEmpty()? s3Service.uploadFile(image): postRequestDTO.getImageUrl();
        BlogPost post = blogPostMapper.toEntity(postRequestDTO);
        post.setUser(currentUser);
        post.setImageUrl(imageUrl);

        BlogPost savedPost = save(post);
        return blogPostMapper.toDto(savedPost);
    }


    /**
     * Updates a blog post
     * @param id the ID of the blog post to update
     * @param postRequestDTO requestDTO the updated post data
     * @return  the updated BlogPostResponseDTO
     * @throws BlogPostNotFoundException if the post is not found
     */
    @Transactional
    public BlogPostResponseDTO updatePost(int id, BlogPostRequestDTO postRequestDTO) {
        logger.info("Updating blog post with ID: {}", id);
        BlogPost existingPost = findById(id)
                .orElseThrow(() -> {
                    logger.error("Post with ID {} not found", id);
                    return new BlogPostNotFoundException("Post not found with id: " + id);
                });
        //check ownership or admin  role
        User currentUser = getCurrentUser();
        if(!existingPost.getUser().getUsername().equals(currentUser.getUsername()) &&
        currentUser.getRole() != Role.ADMIN) {
            logger.warn("User {} attemtpted to update the post {} by Owner id {}", currentUser.getUsername(), id, existingPost.getUser().getUsername());
            throw new UnauthorizedException("You can only update your own posts");
        }
        existingPost.setTitle(postRequestDTO.getTitle());
        existingPost.setContent(postRequestDTO.getContent());
        existingPost.setImageUrl(postRequestDTO.getImageUrl());
        BlogPost updatedPost = save(existingPost);
        return blogPostMapper.toDto(updatedPost);
    }

    @Transactional
    public void deletePost(int id) {
        logger.info("Deleting post by ID: {}", id);
        BlogPost existingPost = findById(id)
                .orElseThrow(() -> {
                    logger.error("Post with ID {} not found", id);
                    return new BlogPostNotFoundException("Post not found with id: " + id);
                });

        User currentUser = getCurrentUser();
        if(!existingPost.getUser().getUsername().equals(currentUser.getUsername()) &&
        currentUser.getRole() != Role.ADMIN) {
            logger.error("User {} attempted to delete post {} by Owner id {}", currentUser.getUsername(), id, existingPost.getUser().getUsername());
            throw new UnauthorizedException("You can only delete your own posts");
        }
        existingPost.setDeleted(true);
        save(existingPost);
        logger.info("Post with ID {} deleted successfully", id);
    }


    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !(authentication.getPrincipal() instanceof User)) {
            throw new BlogPostNotFoundException("User not found");
        }
        return (User) authentication.getPrincipal();
    }
}










//
//
//@Validated is a Spring annotation that enables method-level validation using JSR-380 (Jakarta Bean Validation, formerly javax.validation) annotations like @NotNull, @Valid, @Size, etc.
//
//It is mainly used with:
//
//@Service
//
//@Component
//
//@Controller
//
//So that when a method parameter is annotated with @Valid, Spring can trigger automatic validation before the method logic executes.