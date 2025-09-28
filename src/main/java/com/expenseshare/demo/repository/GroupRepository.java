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

    @Override
    List<Group> findAll();
}
