package com.example.Library_Felix_liden.dto.v2.Book;

import java.util.List;

public record BookListResponse(
        List<BookResponse> data,
        String version
) {
}
