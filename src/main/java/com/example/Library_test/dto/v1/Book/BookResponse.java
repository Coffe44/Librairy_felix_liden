package com.example.Library_test.dto.v1.Book;

import com.example.Library_test.dto.v1.Author.AuthorResponse;

public record BookResponse(
        Long id,
        String title,
        String isbn,
        AuthorResponse author
) {
}
