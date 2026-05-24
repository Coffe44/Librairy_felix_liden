package com.example.Library_Felix_liden.service;

import com.example.Library_Felix_liden.dto.v1.Author.AuthorResponse;
import com.example.Library_Felix_liden.dto.v1.Book.BookRequest;
import com.example.Library_Felix_liden.dto.v1.Book.BookResponse;
import com.example.Library_Felix_liden.entity.Author;
import com.example.Library_Felix_liden.entity.Book;
import com.example.Library_Felix_liden.repository.AuthorRepository;
import com.example.Library_Felix_liden.repository.BookRepository;
import com.example.Library_Felix_liden.repository.LoanRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    @Cacheable(cacheNames = "booksPage", key = "{#pageable.pageNumber, #pageable.pageSize, #pageable.sort.toString()}")
    public Page<BookResponse> findAll(Pageable pageable) {
        return bookRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "booksV2Page", key = "{#pageable.pageNumber, #pageable.pageSize, #pageable.sort.toString()}")
    public Page<com.example.Library_Felix_liden.dto.v2.Book.BookResponse> findAllV2(Pageable pageable) {
        return bookRepository.findAll(pageable)
                .map(this::toV2Response);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "booksById", key = "#id")
    public BookResponse findById(Long id) {
        return toResponse(getBook(id));
    }

    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = "booksByAuthorPage",
            key = "{#authorId, #pageable.pageNumber, #pageable.pageSize, #pageable.sort.toString()}"
    )
    public Page<BookResponse> findByAuthorId(Long authorId, Pageable pageable) {
        if (!authorRepository.existsById(authorId)) {
            throw new NotFoundException("Author with id " + authorId + " was not found");
        }
        return bookRepository.findByAuthorId(authorId, pageable)
                .map(this::toResponse);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "booksPage", allEntries = true),
            @CacheEvict(cacheNames = "booksV2Page", allEntries = true),
            @CacheEvict(cacheNames = "booksByAuthorPage", allEntries = true)
    })
    public BookResponse create(BookRequest request) {
        Author author = getAuthor(request.authorId());
        Book book = new Book(request.title(), request.isbn(), author);
        return toResponse(bookRepository.save(book));
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "booksById", key = "#id"),
            @CacheEvict(cacheNames = "booksPage", allEntries = true),
            @CacheEvict(cacheNames = "booksV2Page", allEntries = true),
            @CacheEvict(cacheNames = "booksByAuthorPage", allEntries = true),
            @CacheEvict(cacheNames = "loansPage", allEntries = true),
            @CacheEvict(cacheNames = "loansById", allEntries = true)
    })
    public BookResponse update(Long id, BookRequest request) {
        Book book = getBook(id);
        Author author = getAuthor(request.authorId());
        book.setTitle(request.title());
        book.setIsbn(request.isbn());
        book.setAuthor(author);
        return toResponse(book);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "booksById", key = "#id"),
            @CacheEvict(cacheNames = "booksPage", allEntries = true),
            @CacheEvict(cacheNames = "booksV2Page", allEntries = true),
            @CacheEvict(cacheNames = "booksByAuthorPage", allEntries = true),
            @CacheEvict(cacheNames = "loansPage", allEntries = true),
            @CacheEvict(cacheNames = "loansById", allEntries = true)
    })
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
