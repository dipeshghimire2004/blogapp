package org.blogapp.dg_blogapp.exception;

public class BlogPostNotFoundException extends RuntimeException {
    private String message;
    public BlogPostNotFoundException(String message) {
        super(message);
    }
}
