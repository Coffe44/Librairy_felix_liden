package com.example.Library_Felix_liden.dto.v1.Loan;

import com.example.Library_Felix_liden.dto.v1.Book.BookResponse;

import java.time.LocalDate;

public record LoanResponse(
        Long id,
        BookResponse book,
        String borrowerName,
        LocalDate loanDate,
        LocalDate returnDate
) {
}
