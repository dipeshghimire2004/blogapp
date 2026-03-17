package org.blogapp.dg_blogapp.repository;

import org.blogapp.dg_blogapp.payment.model.PayoutMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PayoutMethodRepository extends JpaRepository<PayoutMethod, UUID> {

    @Query("SELECT p from PayoutMethod p where p.writer.id = :userId")
    List<PayoutMethod> findByUserId(@Param("userId") UUID userId);
}
