package org.blogapp.dg_blogapp.repository;

import org.blogapp.dg_blogapp.model.BlogPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<BlogPost, Integer> {

}
