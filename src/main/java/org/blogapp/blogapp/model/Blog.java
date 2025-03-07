package org.blogapp.blogapp.model;


import java.time.LocalDateTime;
import jakarta.persistence.*;       //for crud operations (ORM)

@Entity
@Table(name="blog")
public class Blog {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY) //auto increment
    @Column(name="id", nullable =false, updatable=false)
    private long id;
    @Column(name="title", nullable=false)
    private String title;

    @Column(name="content", nullable=false)
    private String content;

    @Column(name="created_at", nullable=false, updatable=false)
    private LocalDateTime created_at;
    @Column(name="updated_at", nullable =false, updatable=true)
    private LocalDateTime updated_at;

   public long getId(){
       return id;
   }

    public void setId(long id){
        this.id = id;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public LocalDateTime getCreated_at() {
        return created_at;
    }
    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }
    public LocalDateTime getUpdated_at() {
        return updated_at;
    }
    public void setUpdated_at(LocalDateTime updated_at) {
       this.updated_at = updated_at;
    }

}
