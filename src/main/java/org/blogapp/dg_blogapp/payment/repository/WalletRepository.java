package org.blogapp.dg_blogapp.payment.repository;

import org.blogapp.dg_blogapp.model.User;
import org.blogapp.dg_blogapp.payment.enums.WalletStatus;
import org.blogapp.dg_blogapp.payment.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    Optional<Wallet> findByUserAndStatus(User user, WalletStatus status);
}
