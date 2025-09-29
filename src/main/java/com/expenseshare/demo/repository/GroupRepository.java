package com.expenseshare.demo.repository;

import com.expenseshare.demo.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {

    Optional<Group> findByName(String name);
    boolean existsByName(String name);

    @Query("SELECT g FROM Group g JOIN g.users u1 JOIN g.users u2 WHERE u1.id = :userId1 AND u2.id = :userId2")
    List<Group> findGroupsWithBothUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
    @Override
    List<Group> findAll();
}
