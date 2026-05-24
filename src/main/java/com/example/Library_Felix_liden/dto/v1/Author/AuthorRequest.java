package com.example.Library_Felix_liden.dto.v1.Author;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthorRequest(
        @NotBlank
        @Size(min = 2, max = 100)
        String name
) {
}
