package org.blogapp.dg_blogapp.payment.repository;

import org.blogapp.dg_blogapp.payment.model.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DonationRepository extends JpaRepository<Donation, UUID>
{
}
