package com.zorysa.finance.bills.repository;

import com.zorysa.finance.bills.entity.Bill;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BillRepository extends JpaRepository<Bill, UUID>, JpaSpecificationExecutor<Bill> {

    Optional<Bill> findByIdAndUserId(UUID id, UUID userId);

    Page<Bill> findAllByUserId(UUID userId, Pageable pageable);

    @Query("""
            select bill
            from Bill bill
            where bill.userId = :userId
              and bill.status = com.zorysa.finance.bills.entity.BillStatus.PENDING
              and bill.dueDate < :currentDate
            """)
    Page<Bill> findOverdueByUserId(
            @Param("userId") UUID userId,
            @Param("currentDate") LocalDate currentDate,
            Pageable pageable
    );

    @Query("""
            select bill
            from Bill bill
            where bill.userId = :userId
              and bill.status = com.zorysa.finance.bills.entity.BillStatus.PENDING
              and bill.dueDate between :startDate and :endDate
            """)
    Page<Bill> findUpcomingByUserId(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );
}
