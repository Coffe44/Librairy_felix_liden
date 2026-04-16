package com.example.Library_test.dto.v1.Loan;

import com.example.Library_test.dto.v1.Book.BookResponse;

import java.time.LocalDate;

public record LoanResponse(
        Long id,
        BookResponse book,
        String borrowerName,
        LocalDate loanDate,
        LocalDate returnDate
) {
}
