package org.blogapp.dg_blogapp.specification;


import jakarta.persistence.criteria.Predicate;
import org.blogapp.dg_blogapp.dto.BlogPostFilterRequest;
import org.blogapp.dg_blogapp.model.BlogPost;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class BlogPostSpecification {
    public static Specification<BlogPost> build(BlogPostFilterRequest filter) {
        return (root, query, cb) ->{
            List<Predicate> predicates = new ArrayList<>();

            if(filter.getTitle() !=null){
                predicates.add(cb.equal(root.get("title"), filter.getTitle().toLowerCase()));
            }
            if(filter.getTags()!=null){
                predicates.add(cb.equal(root.get("tags"), filter.getTags().toLowerCase()));
            }
            if(filter.getFeatured()!=null){
                predicates.add(cb.equal(root.get("isFeatured"), filter.getFeatured()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
