package com.eventphotography.management.repository;

import com.eventphotography.management.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Integer> {
    List<Expense> findByPhotographer_PhotographerID(Integer photographerID);
    List<Expense> findByStatus(String status);
}
