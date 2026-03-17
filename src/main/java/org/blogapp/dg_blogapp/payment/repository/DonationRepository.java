package org.blogapp.dg_blogapp.payment.repository;

import org.blogapp.dg_blogapp.payment.enums.DonationStatus;
import org.blogapp.dg_blogapp.payment.model.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DonationRepository extends JpaRepository<Donation, UUID> {
    List<Donation> findBySenderIdAndStatus(UUID senderId, DonationStatus status);

    // ✨ NEW: Find donations by receiver and status
    List<Donation> findByReceiverIdAndStatus(UUID receiverId, DonationStatus status);

    // ✨ NEW: Find all completed donations (for admin)
    List<Donation> findByStatus(DonationStatus status);

    // ✨ NEW: Count pending acceptance for donor
    @Query("SELECT COUNT(d) FROM Donation d WHERE d.sender.id = :senderId " +
            "AND d.status = 'COMPLETED'")
    long countPendingAcceptanceForDonor(@Param("senderId") UUID senderId);
}
