package org.blogapp.blogapp.model;


import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.persistence.*;       //for crud operations (ORM)
import lombok.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Data   //for getters, setters, hashcode, toString, Equals
@Entity
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)      //enables auditing


@Table(name="blog")
public class Blog {
    @Id
    @GeneratedValue(strategy =GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private int id;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name="photo", length=255)
    private String photoUrl;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime created_at;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false, updatable = true)
    private LocalDateTime updated_at;


}