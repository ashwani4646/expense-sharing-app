package com.expenseshare.demo.repository;

import com.expenseshare.demo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserName(String userName);
    Optional<User> findByEmailId(String emailId);
    Optional<User> findByProviderAndProviderId(String provider, String providerId);
    Page<User> findAll(Pageable pageable);
    boolean existsByUserName(String userName);
    boolean existsByEmailId(String emailId);
}
