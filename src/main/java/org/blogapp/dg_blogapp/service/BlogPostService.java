package org.blogapp.dg_blogapp.service;

import com.amazonaws.services.connect.model.UserNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.blogapp.dg_blogapp.dto.BlogPostFilterRequest;
import org.blogapp.dg_blogapp.dto.BlogPostRequestDTO;
import org.blogapp.dg_blogapp.dto.BlogPostResponseDTO;
import org.blogapp.dg_blogapp.dto.PageResponse;
import org.blogapp.dg_blogapp.exception.BlogPostNotFoundException;
import org.blogapp.dg_blogapp.exception.UnauthorizedException;
import org.blogapp.dg_blogapp.mapper.BlogPostMapper;
import org.blogapp.dg_blogapp.model.BlogPost;
import org.blogapp.dg_blogapp.model.Role;
import org.blogapp.dg_blogapp.model.User;
import org.blogapp.dg_blogapp.repository.PostRepository;
import org.blogapp.dg_blogapp.repository.UserRepository;
import org.blogapp.dg_blogapp.specification.BlogPostSpecification;
import org.blogapp.dg_blogapp.utils.FileNameGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
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
    private final JwtService jwtService;

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



    public PageResponse<BlogPostResponseDTO> getPosts(int pageNo, int pageSize, String sortBy, String sortOrder) {
        //validation
        if(pageNo<0) pageNo = 0;
        if(pageSize<1 || pageSize>80) pageSize = 10;

        //Create sort
        Sort sort= sortOrder.equalsIgnoreCase("desc")? Sort.by(sortBy).descending(): Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(pageNo, pageSize,sort);
        Page<BlogPost> blogPosts = postRepository.findAll(pageable);

        //Convert entity to dto
        List<BlogPostResponseDTO> postResponses = blogPostMapper.toDtoList(blogPosts.getContent());

        // Convert entity page to DTO page
        return new PageResponse<>(blogPosts, postResponses);
    }

    public PageResponse<BlogPostResponseDTO> getPostsByTitle(String title, int pageNo, int pageSize) {
        if(pageNo<0) pageNo = 0;
        if(pageSize<1 || pageSize>80) pageSize = 10;

        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("createdAt").descending());
        Page<BlogPost> blogPosts = postRepository.findByTitle(title, pageable);

        List<BlogPostResponseDTO> postResponses = blogPostMapper.toDtoList(blogPosts.getContent());

        return new PageResponse<>(blogPosts, postResponses);
    }

    public List<BlogPostResponseDTO> getPostsForSearch(BlogPostFilterRequest filter) {
//        if(pageNo<0) pageNo = 0;
//        if(pageSize<1 || pageSize>80) pageSize = 10;

//        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("createdAt").descending());
        List<BlogPost> blogPosts = postRepository.findAll(BlogPostSpecification.build(filter));

//        List<BlogPostResponseDTO> postResponses = blogPostMapper.toDtoList(blogPosts.getContent());

        return blogPostMapper.toDtoList(blogPosts);

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
    public BlogPostResponseDTO getPostById(UUID id) {
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
    public BlogPostResponseDTO updatePost(UUID id, BlogPostRequestDTO postRequestDTO, MultipartFile image) {
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

        String imageUrl = uploadImageFile(currentUser.getId(), image);
        existingPost.setTitle(postRequestDTO.getTitle());
        existingPost.setContent(postRequestDTO.getContent());
        existingPost.setImageUrl(imageUrl);
        BlogPost updatedPost = postRepository.save(existingPost);
        return blogPostMapper.toDto(updatedPost);
    }

    @Transactional
    public void deletePost(UUID id) {
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
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        if (principal instanceof Long) {
            Long userId = (Long) principal;
            return userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));
        }
        
        throw new UserNotFoundException("Unable to determine current user");
    }
//    private User getCurrentUser() {
//        log.info("Getting current user");
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if(authentication == null || !(authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken)) {
//            log.info("User not logged in");
//            throw new BlogPostNotFoundException("User not authenticated");
//        }
//        String username = authentication.getName();
//        return userRepository.findByUsername(username).orElseThrow(() -> new BlogPostNotFoundException("User not found"));
//    }

    private String uploadImageFile(Long userId, MultipartFile image) {
        String path = String.format("users/%s/images/", userId);
        String key = fileNameGenerator.generateProductImageName(image);

        String fullPath = path+key;

        s3Service.uploadFileIntoS3(image,fullPath);
        return fullPath;
    }
}
