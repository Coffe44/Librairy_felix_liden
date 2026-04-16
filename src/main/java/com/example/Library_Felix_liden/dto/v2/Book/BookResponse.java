package com.example.Library_Felix_liden.dto.v2.Book;

import com.example.Library_Felix_liden.dto.v2.Author.AuthorResponse;

public record BookResponse(
        Long id,
        String title,
        String isbn,
        AuthorResponse author,
        boolean available
) {
}
