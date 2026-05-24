package com.example.Library_Felix_liden.service;

import com.example.Library_Felix_liden.dto.v1.Author.AuthorResponse;
import com.example.Library_Felix_liden.dto.v1.Book.BookResponse;
import com.example.Library_Felix_liden.dto.v1.Loan.LoanRequest;
import com.example.Library_Felix_liden.dto.v1.Loan.LoanResponse;
import com.example.Library_Felix_liden.entity.Book;
import com.example.Library_Felix_liden.entity.Loan;
import com.example.Library_Felix_liden.repository.BookRepository;
import com.example.Library_Felix_liden.repository.LoanRepository;
import java.time.LocalDate;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoanService {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;

    public LoanService(LoanRepository loanRepository, BookRepository bookRepository) {
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "loansPage", key = "{#pageable.pageNumber, #pageable.pageSize, #pageable.sort.toString()}")
    public Page<LoanResponse> findAll(Pageable pageable) {
        return loanRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "loansById", key = "#id")
    public LoanResponse findById(Long id) {
        return toResponse(getLoan(id));
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "loansPage", allEntries = true),
            @CacheEvict(cacheNames = "booksV2Page", allEntries = true)
    })
    public LoanResponse create(LoanRequest request) {
        Book book = getBookForLoanCreation(request.bookId());
        if (loanRepository.existsByBookIdAndReturnDateIsNull(book.getId())) {
            throw new BusinessRuleException("Book with id " + book.getId() + " is already loaned");
        }
        Loan loan = new Loan(book, request.borrowerName(), LocalDate.now());
        return toResponse(loanRepository.save(loan));
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "loansById", key = "#id"),
            @CacheEvict(cacheNames = "loansPage", allEntries = true),
            @CacheEvict(cacheNames = "booksV2Page", allEntries = true)
    })
    public LoanResponse returnLoan(Long id) {
        Loan loan = getLoan(id);
        if (loan.getReturnDate() != null) {
            throw new BusinessRuleException("Loan with id " + id + " has already been returned");
        }
        loan.setReturnDate(LocalDate.now());
        return toResponse(loan);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "loansById", key = "#id"),
            @CacheEvict(cacheNames = "loansPage", allEntries = true),
            @CacheEvict(cacheNames = "booksV2Page", allEntries = true)
    })
    public void delete(Long id) {
        Loan loan = getLoan(id);
        loanRepository.delete(loan);
    }

    private Loan getLoan(Long id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Loan with id " + id + " was not found"));
    }

    private Book getBookForLoanCreation(Long id) {
        return bookRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new NotFoundException("Book with id " + id + " was not found"));
    }

    private LoanResponse toResponse(Loan loan) {
        return new LoanResponse(
                loan.getId(),
                toBookResponse(loan.getBook()),
                loan.getBorrowerName(),
                loan.getLoanDate(),
                loan.getReturnDate()
        );
    }

    private BookResponse toBookResponse(Book book) {
        AuthorResponse author = new AuthorResponse(
                book.getAuthor().getId(),
                book.getAuthor().getName()
        );
        return new BookResponse(book.getId(), book.getTitle(), book.getIsbn(), author);
    }
}
