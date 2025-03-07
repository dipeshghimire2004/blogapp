package org.blogapp.blogapp.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.blogapp.blogapp.model.Blog;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {

}
