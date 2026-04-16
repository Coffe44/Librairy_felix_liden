package com.example.Library_Felix_liden.repository;

import com.example.Library_Felix_liden.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorRepository extends JpaRepository<Author, Long> {
}
