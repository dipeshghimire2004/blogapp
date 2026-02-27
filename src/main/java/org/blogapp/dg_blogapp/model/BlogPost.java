package org.blogapp.dg_blogapp.model;


import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "blog_posts")
//for auditing (requires spring-data-jpa dependency and @EnableJpaAuditing in a config class).
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder        //added for cleaner  object creation
@SQLRestriction("deleted=false")  // Only fetch non-deleted posts
public class BlogPost extends BaseEntity
{


    @Column(name = "title", length = 100, nullable = false)
    @NotBlank(message = "title cannot be blank")
    @Size(max = 100, message = "title must not exceed 100 characters")
    private String title;


    @Lob        //large text
    @Basic(fetch = FetchType.EAGER)
    @Column(name = "content")
    private String content;


    @Column(name = "image_url", length = 255)
    @Size(max = 255, message = "Image URL must not exceed 155 characters")
    private String imageUrl;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private boolean featured = true;

    private String tags;


    @Builder.Default
    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;
}
