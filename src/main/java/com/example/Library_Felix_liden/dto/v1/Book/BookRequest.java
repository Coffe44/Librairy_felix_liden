package com.example.Library_Felix_liden.dto.v1.Book;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record BookRequest(
        @NotBlank
        @Size(max = 200)
        String title,
        @Size(min = 10, max = 17)
        @Pattern(regexp = "^(?:[0-9-]+)?$", message = "must contain only digits or hyphens")
        String isbn,
        @NotNull
        @Positive
        Long authorId
) {
}
