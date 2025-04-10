package org.blogapp.dg_blogapp.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * Generic service providing basic CRUD operations for any entity.
 * @param <T> The entity type (e.g., BlogPost, User)
 * @param <ID> The entity's ID type (e.g., Integer, Long)
 */
public abstract class GenericService<T, ID> {
    protected final JpaRepository<T, ID> repository;


    protected GenericService(JpaRepository<T, ID> repository) {
        this.repository = repository;
    }

    public List<T> findAll() {
        return repository.findAll();
    }

    public T findById(ID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("No record found with id: " + id));
    }

    @Transactional
    public T save(T entity) {
        return repository.save(entity);
    }

    @Transactional
    public void delete(ID id) {
        T entity = findById(id);
        repository.delete(entity);
    }

}
