package com.example.Library_Felix_liden.dto.v1.Book;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BookRequest(
        @NotBlank String title,
        String isbn,
        @NotNull Long authorId
) {
}
