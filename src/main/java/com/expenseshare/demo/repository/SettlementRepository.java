package com.expenseshare.demo.repository;

import com.expenseshare.demo.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
}
