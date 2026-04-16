package com.example.Library_test.service;

import com.example.Library_test.dto.v1.Author.AuthorRequest;
import com.example.Library_test.dto.v1.Author.AuthorResponse;
import com.example.Library_test.entity.Author;
import com.example.Library_test.repository.AuthorRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthorService {

    private final AuthorRepository authorRepository;

    public AuthorService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    @Transactional(readOnly = true)
    public List<AuthorResponse> findAll() {
        return authorRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AuthorResponse findById(Long id) {
        return toResponse(getAuthor(id));
    }

    @Transactional
    public AuthorResponse create(AuthorRequest request) {
        return toResponse(authorRepository.save(new Author(request.name())));
    }

    @Transactional
    public AuthorResponse update(Long id, AuthorRequest request) {
        Author author = getAuthor(id);
        author.setName(request.name());
        return toResponse(author);
    }

    @Transactional
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
