package com.example.Library_Felix_liden.dto.v1.Loan;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record LoanRequest(
        @NotNull
        @Positive
        Long bookId,
        @NotBlank
        @Size(min = 2, max = 100)
        String borrowerName
) {
}
