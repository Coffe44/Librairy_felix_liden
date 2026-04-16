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
import java.util.List;
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
    public List<LoanResponse> findAll() {
        return loanRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public LoanResponse findById(Long id) {
        return toResponse(getLoan(id));
    }

    @Transactional
    public LoanResponse create(LoanRequest request) {
        Book book = getBookForLoanCreation(request.bookId());
        if (loanRepository.existsByBookIdAndReturnDateIsNull(book.getId())) {
            throw new BusinessRuleException("Book with id " + book.getId() + " is already loaned");
        }
        Loan loan = new Loan(book, request.borrowerName(), LocalDate.now());
        return toResponse(loanRepository.save(loan));
    }

    @Transactional
    public LoanResponse returnLoan(Long id) {
        Loan loan = getLoan(id);
        if (loan.getReturnDate() != null) {
            throw new BusinessRuleException("Loan with id " + id + " has already been returned");
        }
        loan.setReturnDate(LocalDate.now());
        return toResponse(loan);
    }

    @Transactional
    public void delete(Long id) {
        Loan loan = getLoan(id);
        loanRepository.delete(loan);
    }

    private Loan getLoan(Long id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Loan with id " + id + " was not found"));
    }

    private Book getBook(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Book with id " + id + " was not found"));
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
