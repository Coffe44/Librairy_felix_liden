package com.example.Library_Felix_liden.service;

import com.example.Library_Felix_liden.dto.v1.Author.AuthorResponse;
import com.example.Library_Felix_liden.dto.v1.Book.BookRequest;
import com.example.Library_Felix_liden.dto.v1.Book.BookResponse;
import com.example.Library_Felix_liden.entity.Author;
import com.example.Library_Felix_liden.entity.Book;
import com.example.Library_Felix_liden.repository.AuthorRepository;
import com.example.Library_Felix_liden.repository.BookRepository;
import com.example.Library_Felix_liden.repository.LoanRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final LoanRepository loanRepository;

    public BookService(BookRepository bookRepository, AuthorRepository authorRepository, LoanRepository loanRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.loanRepository = loanRepository;
    }

    @Transactional(readOnly = true)
    public List<BookResponse> findAll() {
        return bookRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<com.example.Library_Felix_liden.dto.v2.Book.BookResponse> findAllV2() {
        return bookRepository.findAll().stream()
                .map(this::toV2Response)
                .toList();
    }

    @Transactional(readOnly = true)
    public BookResponse findById(Long id) {
        return toResponse(getBook(id));
    }

    @Transactional(readOnly = true)
    public List<BookResponse> findByAuthorId(Long authorId) {
        if (!authorRepository.existsById(authorId)) {
            throw new NotFoundException("Author with id " + authorId + " was not found");
        }
        return bookRepository.findByAuthorId(authorId).stream()
                .map(this::toResponse)
                .toList();
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

    private com.example.Library_Felix_liden.dto.v2.Book.BookResponse toV2Response(Book book) {
        com.example.Library_Felix_liden.dto.v2.Author.AuthorResponse author =
                new com.example.Library_Felix_liden.dto.v2.Author.AuthorResponse(
                        book.getAuthor().getId(),
                        book.getAuthor().getName()
                );
        boolean available = !loanRepository.existsByBookIdAndReturnDateIsNull(book.getId());
        return new com.example.Library_Felix_liden.dto.v2.Book.BookResponse(
                book.getId(),
                book.getTitle(),
                book.getIsbn(),
                author,
                available
        );
    }

    private Author getAuthor(Long id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Author with id " + id + " was not found"));
    }

    private AuthorResponse toAuthorResponse(Author author) {
        return new AuthorResponse(author.getId(), author.getName());
    }
}
