package com.example.Library_Felix_liden.controller;

import com.example.Library_Felix_liden.dto.v2.Book.BookListResponse;
import com.example.Library_Felix_liden.service.BookService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
    public BookListResponse findAll(@PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return BookListResponse.from(bookService.findAllV2(pageable), "v2");
    }
}
