package com.example.Library_Felix_liden.repository;

import com.example.Library_Felix_liden.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    boolean existsByBookIdAndReturnDateIsNull(Long bookId);

    long countByBookIdAndReturnDateIsNull(Long bookId);
}
