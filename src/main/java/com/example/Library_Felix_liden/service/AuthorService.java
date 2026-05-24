package com.example.Library_Felix_liden.service;

import com.example.Library_Felix_liden.dto.v1.Author.AuthorRequest;
import com.example.Library_Felix_liden.dto.v1.Author.AuthorResponse;
import com.example.Library_Felix_liden.entity.Author;
import com.example.Library_Felix_liden.repository.AuthorRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthorService {

    private final AuthorRepository authorRepository;

    public AuthorService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "authorsPage", key = "{#pageable.pageNumber, #pageable.pageSize, #pageable.sort.toString()}")
    public Page<AuthorResponse> findAll(Pageable pageable) {
        return authorRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "authorsById", key = "#id")
    public AuthorResponse findById(Long id) {
        return toResponse(getAuthor(id));
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "authorsPage", allEntries = true),
            @CacheEvict(cacheNames = "booksById", allEntries = true),
            @CacheEvict(cacheNames = "booksPage", allEntries = true),
            @CacheEvict(cacheNames = "booksByAuthorPage", allEntries = true),
            @CacheEvict(cacheNames = "booksV2Page", allEntries = true)
    })
    public AuthorResponse create(AuthorRequest request) {
        return toResponse(authorRepository.save(new Author(request.name())));
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "authorsById", key = "#id"),
            @CacheEvict(cacheNames = "authorsPage", allEntries = true),
            @CacheEvict(cacheNames = "booksById", allEntries = true),
            @CacheEvict(cacheNames = "booksPage", allEntries = true),
            @CacheEvict(cacheNames = "booksByAuthorPage", allEntries = true),
            @CacheEvict(cacheNames = "booksV2Page", allEntries = true)
    })
    public AuthorResponse update(Long id, AuthorRequest request) {
        Author author = getAuthor(id);
        author.setName(request.name());
        return toResponse(author);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "authorsById", key = "#id"),
            @CacheEvict(cacheNames = "authorsPage", allEntries = true),
            @CacheEvict(cacheNames = "booksById", allEntries = true),
            @CacheEvict(cacheNames = "booksPage", allEntries = true),
            @CacheEvict(cacheNames = "booksByAuthorPage", allEntries = true),
            @CacheEvict(cacheNames = "booksV2Page", allEntries = true)
    })
    public void delete(Long id) {
        Author author = getAuthor(id);
        authorRepository.delete(author);
    }

    private Author getAuthor(Long id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Author with id " + id + " was not found"));
    }

    private AuthorResponse toResponse(Author author) {
        return new AuthorResponse(author.getId(), author.getName());
    }
}
