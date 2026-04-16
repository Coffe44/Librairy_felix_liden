package com.example.Library_test.dto.v1.Loan;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoanRequest(
        @NotNull Long bookId,
        @NotBlank String borrowerName
) {
}
