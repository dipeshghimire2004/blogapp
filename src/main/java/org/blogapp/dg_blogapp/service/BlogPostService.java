package org.blogapp.dg_blogapp.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.blogapp.dg_blogapp.utils.FileNameGenerator;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
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
@RequiredArgsConstructor
@Slf4j
public class BlogPostService {

    private final PostRepository postRepository;

    private final BlogPostMapper blogPostMapper;

    private final S3Service s3Service;

    private final UserRepository userRepository;

    private final FileNameGenerator fileNameGenerator;

    /**
     * Creates a new blog post
     * @param postRequestDTO the blog post request data
     * @param image the URL of the uploaded image
     * @return the created BlogPostResponseDTO
     */
    @Transactional
    public BlogPostResponseDTO createPost(MultipartFile image, @Valid BlogPostRequestDTO postRequestDTO) {
        log.info("Creating new blog post with title: {}", postRequestDTO.getTitle());
        User currentUser= getCurrentUser();



        String imageUrl = uploadImageFile(currentUser.getId(), image);
        BlogPost post = blogPostMapper.toEntity(postRequestDTO);
        post.setUser(currentUser);
        post.setImageUrl(imageUrl);

        BlogPost savedPost = postRepository.save(post);
        return blogPostMapper.toDto(savedPost);
    }




    /**
     * retrieves all the blogpost
     * @return a list of blogResponseDTO
     */
    public List<BlogPostResponseDTO> getAllPosts() {
        log.info("Fetching  all blog posts");
        return postRepository.findAll().stream()
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
        log.info("Fetching post with ID: {}", id);
        BlogPost post = postRepository.findById(id)
                .orElseThrow(() -> new BlogPostNotFoundException("Post not found with id: " + id));
        return blogPostMapper.toDto(post);
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
        log.info("Updating blog post with ID: {}", id);
        BlogPost existingPost = postRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Post with ID {} not found", id);
                    return new BlogPostNotFoundException("Post not found with id: " + id);
                });
        //check ownership or admin  role
        User currentUser = getCurrentUser();
        if(!existingPost.getUser().getUsername().equals(currentUser.getUsername()) &&
        currentUser.getRole() != Role.ADMIN) {
            log.warn("User {} attemtpted to update the post {} by Owner id {}", currentUser.getUsername(), id, existingPost.getUser().getUsername());
            throw new UnauthorizedException("You can only update your own posts");
        }
        existingPost.setTitle(postRequestDTO.getTitle());
        existingPost.setContent(postRequestDTO.getContent());
        existingPost.setImageUrl(postRequestDTO.getImageUrl());
        BlogPost updatedPost = postRepository.save(existingPost);
        return blogPostMapper.toDto(updatedPost);
    }

    @Transactional
    public void deletePost(int id) {
        log.info("Deleting post by ID: {}", id);
        BlogPost existingPost = postRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Post with ID {} not found", id);
                    return new BlogPostNotFoundException("Post not found with id: " + id);
                });

        User currentUser = getCurrentUser();
        if(!existingPost.getUser().getUsername().equals(currentUser.getUsername()) &&
        currentUser.getRole() != Role.ADMIN) {
            log.error("User {} attempted to delete post {} by Owner id {}", currentUser.getUsername(), id, existingPost.getUser().getUsername());
            throw new UnauthorizedException("You can only delete your own posts");
        }
        existingPost.setDeleted(true);
        postRepository.save(existingPost);
        log.info("Post with ID {} deleted successfully", id);
    }


    private User getCurrentUser() {
        log.info("Getting current user");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !(authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken)) {
            log.info("User not logged in");
            throw new BlogPostNotFoundException("User not authenticated");
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username).orElseThrow(() -> new BlogPostNotFoundException("User not found"));
    }

    private String uploadImageFile(Long userId, MultipartFile image) {
        String path = String.format("users/%s/images/", userId);
        String key = fileNameGenerator.generateProductImageName(image);

        String fullPath = path+key;

        s3Service.uploadFileIntoS3(image,fullPath);
        return fullPath;
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