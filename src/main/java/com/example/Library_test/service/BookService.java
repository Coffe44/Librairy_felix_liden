package com.example.Library_test.service;

import com.example.Library_test.dto.v1.Author.AuthorResponse;
import com.example.Library_test.dto.v1.Book.BookRequest;
import com.example.Library_test.dto.v1.Book.BookResponse;
import com.example.Library_test.entity.Author;
import com.example.Library_test.entity.Book;
import com.example.Library_test.repository.AuthorRepository;
import com.example.Library_test.repository.BookRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;

    public BookService(BookRepository bookRepository, AuthorRepository authorRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
    }

    @Transactional(readOnly = true)
    public List<BookResponse> findAll() {
        return bookRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BookResponse findById(Long id) {
        return toResponse(getBook(id));
    }

    @Transactional
    public BookResponse create(BookRequest request) {
        Author author = getAuthor(request.authorId());
        Book book = new Book(request.title(), request.isbn(), author);
        return toResponse(bookRepository.save(book));
    }

    @Transactional
    public BookResponse update(Long id, BookRequest request) {
        Book book = getBook(id);
        Author author = getAuthor(request.authorId());
        book.setTitle(request.title());
        book.setIsbn(request.isbn());
        book.setAuthor(author);
        return toResponse(book);
    }

    @Transactional
    public void delete(Long id) {
        Book book = getBook(id);
        bookRepository.delete(book);
    }

    private Book getBook(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Book with id " + id + " was not found"));
    }

    private BookResponse toResponse(Book book) {
        AuthorResponse author = toAuthorResponse(book.getAuthor());
        return new BookResponse(book.getId(), book.getTitle(), book.getIsbn(), author);
    }

    private Author getAuthor(Long id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Author with id " + id + " was not found"));
    }

    private AuthorResponse toAuthorResponse(Author author) {
        return new AuthorResponse(author.getId(), author.getName());
    }
}
