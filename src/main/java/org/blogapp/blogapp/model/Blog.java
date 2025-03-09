package org.blogapp.blogapp.model;


import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.persistence.*;       //for crud operations (ORM)
import lombok.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data   //for getters, setters, hashcode, toString, Equals
@Entity
@NoArgsConstructor
//@Getter
//@Setter
//@AllArgsConstructor
//@Builder

@Table(name="blog")
public class Blog {
    @Id
//    @GeneratedValue(strategy=GenerationType.IDENTITY) //auto increment
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id", nullable =false, updatable=false)
    private int id;

    @Column(name="title", nullable=false,length=150)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name="created_at", nullable=false, updatable=false)
    private LocalDateTime created_at;


    @Column(name="updated_at", nullable =false, updatable=true)
    private LocalDateTime updated_at;


    @PrePersist
    protected void onCreate(){
        created_at=LocalDateTime.now();
        updated_at=LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate(){
        updated_at=LocalDateTime.now();
    }

//
//   public int getId(){
//       return id;
//   }
//
//    public void setId(int id){
//        this.id = id;
//    }
//
//    public String getTitle() {
//        return title;
//    }
//    public void setTitle(String title) {
//        this.title = title;
//    }
//    public String getContent() {
//        return content;
//    }
//    public void setContent(String content) {
//        this.content = content;
//    }
//    public LocalDateTime getCreated_at() {
//        return created_at;
//    }
//    public void setCreated_at(LocalDateTime created_at) {
//        this.created_at = created_at;
//    }
//    public LocalDateTime getUpdated_at() {
//        return updated_at;
//    }
//    public void setUpdated_at(LocalDateTime updated_at) {
//       this.updated_at = updated_at;
//    }

}
