package com.expenseshare.demo.repository;


import com.expenseshare.demo.entity.UserBalance;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBalanceRepository extends JpaRepository<UserBalance, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ub FROM UserBalance ub WHERE " +
            "((ub.debtor.id = :userId1 AND ub.creditor.id = :userId2) OR " +
            "(ub.debtor.id = :userId2 AND ub.creditor.id = :userId1)) AND " +
            "ub.group.id = :groupId AND ub.amount > 0")
    List<UserBalance> findBalancesBetweenUsersInGroupForUpdate(
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2,
            @Param("groupId") Long groupId);

    @Query("SELECT ub FROM UserBalance ub WHERE " +
            "ub.debtor.id = :userId OR ub.creditor.id = :userId")
    List<UserBalance> findAllBalancesForUser(@Param("userId") Long userId);

    @Query("SELECT DISTINCT ub.group.id FROM UserBalance ub WHERE " +
            "((ub.debtor.id = :userId1 AND ub.creditor.id = :userId2) OR " +
            "(ub.debtor.id = :userId2 AND ub.creditor.id = :userId1)) AND " +
            "ub.amount > 0")
    List<Long> findGroupsWithBalancesBetweenUsers(
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2);

    Optional<UserBalance> findByGroupIdAndDebtorIdAndCreditorId(
            Long groupId, Long debtorId, Long creditorId);
    @Query("SELECT ub FROM UserBalance ub WHERE ub.group.id = :groupId")
    List<UserBalance> findByGroupId(@Param("groupId") Long groupId);
}
