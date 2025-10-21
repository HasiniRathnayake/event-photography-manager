package com.eventphotography.management.repository;

import com.eventphotography.management.entity.MonthlyFinancialReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface MonthlyFinancialReportRepository extends JpaRepository<MonthlyFinancialReport, Integer> {
    Optional<MonthlyFinancialReport> findByReportMonthAndReportYear(Integer month, Integer year);
}