package org.blogapp.blogapp.service;

import lombok.RequiredArgsConstructor;
import org.blogapp.blogapp.dto.BlogDTO;
import org.blogapp.blogapp.dto.BlogRequestDTO;
import org.blogapp.blogapp.exception.ResourceNotFoundException;
import org.blogapp.blogapp.mappper.BlogMapper;
import org.blogapp.blogapp.model.Blog;
import org.blogapp.blogapp.repository.BlogRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BlogService {

    private final BlogRepository blogRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.upload-dir:/default-image}")
    private String defaultImage;

    private String saveFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        // Create the upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            try {
                Files.createDirectory(uploadPath);
            } catch (IOException e) {
                throw new IOException("Failed to create upload directory: " + e.getMessage(), e);
            }
        }
        String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);

        // Save the file
        try {
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IOException("Failed to save file: " + e.getMessage(), e);
        }
        return fileName;
    }

    public BlogDTO createBlog(BlogRequestDTO blogRequestDTO) throws IOException {
        Blog blog = new Blog();
        blog.setTitle(blogRequestDTO.getTitle());

        // Save the uploaded photo
        String photoUrl = saveFile(blogRequestDTO.getPhoto());
        blog.setPhotoUrl(photoUrl != null ? photoUrl : defaultImage);
        blog.setContent(blogRequestDTO.getContent());

        blog = blogRepository.save(blog);
        return BlogMapper.INSTANCE.toDTO(blog);
    }

    @Transactional(readOnly = true)
    public List<BlogDTO> getAllBlogs() {
        return blogRepository.findAll().stream().map(BlogMapper.INSTANCE::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BlogDTO getBlogById(int id) {
        Optional<Blog> blog = blogRepository.findById(id);
        return blog.map(BlogMapper.INSTANCE::toDTO).orElseThrow(() -> new ResourceNotFoundException("Blog not found"));
    }

    public BlogDTO updateBlog(int id, BlogRequestDTO blogRequestDTO) throws IOException {
        Blog blog = blogRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Blog not found"));
        if (blogRequestDTO.getTitle() != null) {
            blog.setTitle(blogRequestDTO.getTitle());
        }
        if (blogRequestDTO.getPhoto() != null && !blogRequestDTO.getPhoto().isEmpty()) {
            String photoUrl = saveFile(blogRequestDTO.getPhoto());
            blog.setPhotoUrl(photoUrl != null ? photoUrl : defaultImage);
        }
        if (blogRequestDTO.getContent() != null) {
            blog.setContent(blogRequestDTO.getContent());
        }
        blog = blogRepository.save(blog);
        return BlogMapper.INSTANCE.toDTO(blog);
    }

    public void deleteBlog(int id) {
        Blog blog = blogRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Blog not found"));
        blogRepository.delete(blog);
    }
}