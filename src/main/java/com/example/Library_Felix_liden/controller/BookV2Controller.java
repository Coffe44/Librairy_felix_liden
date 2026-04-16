package com.example.Library_Felix_liden.controller;

import com.example.Library_Felix_liden.dto.v2.Book.BookListResponse;
import com.example.Library_Felix_liden.service.BookService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/books")
public class BookV2Controller {

    private final BookService bookService;

    public BookV2Controller(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public BookListResponse findAll() {
        return new BookListResponse(bookService.findAllV2(), "v2");
    }
}
