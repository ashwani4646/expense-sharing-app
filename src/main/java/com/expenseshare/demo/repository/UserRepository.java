package com.expenseshare.demo.repository;

import com.expenseshare.demo.entity.Group;
import com.expenseshare.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
