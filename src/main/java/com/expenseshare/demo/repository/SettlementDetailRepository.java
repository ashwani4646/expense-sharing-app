package com.expenseshare.demo.repository;

import com.expenseshare.demo.entity.SettlementDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettlementDetailRepository extends JpaRepository<SettlementDetail, Long> {
}
