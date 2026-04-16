package com.example.Library_Felix_liden.dto.v1.Author;

import jakarta.validation.constraints.NotBlank;

public record AuthorRequest(
        @NotBlank String name
) {
}
