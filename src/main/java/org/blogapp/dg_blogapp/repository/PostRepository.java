package org.blogapp.dg_blogapp.repository;

import org.blogapp.dg_blogapp.model.BlogPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<BlogPost, UUID>, JpaSpecificationExecutor<BlogPost> {

    Page<BlogPost> findAll(Pageable pageable);

    Page<BlogPost> findByTitle(String title, Pageable pageable);

}
