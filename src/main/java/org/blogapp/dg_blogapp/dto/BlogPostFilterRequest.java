package org.blogapp.dg_blogapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BlogPostFilterRequest {
    private String title;
    private String tags;
    private Boolean featured;
}
