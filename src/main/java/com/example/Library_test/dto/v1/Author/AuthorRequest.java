package com.example.Library_test.dto.v1.Author;

import jakarta.validation.constraints.NotBlank;

public record AuthorRequest(
        @NotBlank String name
) {
}
