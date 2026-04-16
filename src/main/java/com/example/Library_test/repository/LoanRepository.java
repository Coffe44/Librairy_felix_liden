package com.example.Library_test.repository;

import com.example.Library_test.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    boolean existsByBookIdAndReturnDateIsNull(Long bookId);

    long countByBookIdAndReturnDateIsNull(Long bookId);
}
