package com.expenseshare.demo.repository;

import com.expenseshare.demo.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    @Query("SELECT s FROM Settlement s WHERE s.payer.id = :userId OR s.receiver.id = :userId " +
            "ORDER BY s.settlementDate DESC")
    List<Settlement> findSettlementsByUser(@Param("userId") Long userId);

    @Query("SELECT s FROM Settlement s WHERE " +
            "(s.payer.id = :userId1 AND s.receiver.id = :userId2) OR " +
            "(s.payer.id = :userId2 AND s.receiver.id = :userId1) " +
            "ORDER BY s.settlementDate DESC")
    List<Settlement> findSettlementsBetweenUsers(
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2);
}

