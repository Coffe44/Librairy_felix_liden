package com.example.Library_test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.Library_test.repository.AuthorRepository;
import com.example.Library_test.repository.BookRepository;
import com.example.Library_test.repository.LoanRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class LibraryApiIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @BeforeEach
    void cleanDatabase() {
        loanRepository.deleteAll();
        bookRepository.deleteAll();
        authorRepository.deleteAll();
    }

    @Test
    void createsAuthorAndListsAuthors() throws Exception {
        mockMvc.perform(post("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Astrid Lindgren"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Astrid Lindgren"));

        mockMvc.perform(get("/api/authors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].name").value("Astrid Lindgren"));
    }

    @Test
    void returnsNotFoundWhenBookDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/books/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Book with id 999 was not found"));
    }

    @Test
    void rejectsDuplicateActiveLoanAndAllowsNewLoanAfterReturn() throws Exception {
        long authorId = createAuthor("Ursula K. Le Guin");
        long bookId = createBook("A Wizard of Earthsea", "9780547773742", authorId);

        long loanId = createLoan(bookId, "Felix");

        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bookId": %d,
                                  "borrowerName": "Anna"
                                }
                                """.formatted(bookId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Book with id " + bookId + " is already loaned"));

        mockMvc.perform(patch("/api/loans/{id}/return", loanId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.returnDate").exists());

        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bookId": %d,
                                  "borrowerName": "Anna"
                                }
                                """.formatted(bookId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.book.id").value(bookId))
                .andExpect(jsonPath("$.borrowerName").value("Anna"));
    }

    @Test
    void createsOnlyOneActiveLoanWhenTwoRequestsRunConcurrently() throws Exception {
        long authorId = createAuthor("Tove Jansson");
        long bookId = createBook("Moominpappa at Sea", "9780312608897", authorId);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Callable<Integer> loanRequest = () -> {
            start.await();
            MvcResult result = mockMvc.perform(post("/api/loans")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "bookId": %d,
                                      "borrowerName": "%s"
                                    }
                                    """.formatted(bookId, Thread.currentThread().getName())))
                    .andReturn();
            return result.getResponse().getStatus();
        };

        Future<Integer> first = executor.submit(loanRequest);
        Future<Integer> second = executor.submit(loanRequest);
        start.countDown();

        List<Integer> statuses = List.of(first.get(), second.get());
        executor.shutdown();

        org.assertj.core.api.Assertions.assertThat(statuses)
                .containsExactlyInAnyOrder(201, 400);
        org.assertj.core.api.Assertions.assertThat(loanRepository.countByBookIdAndReturnDateIsNull(bookId))
                .isEqualTo(1);
    }

    private long createAuthor(String name) throws Exception {
        String response = mockMvc.perform(post("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s"
                                }
                                """.formatted(name)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return readId(response);
    }

    private long createBook(String title, String isbn, long authorId) throws Exception {
        String response = mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "%s",
                                  "isbn": "%s",
                                  "authorId": %d
                                }
                                """.formatted(title, isbn, authorId)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return readId(response);
    }

    private long createLoan(long bookId, String borrowerName) throws Exception {
        String response = mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bookId": %d,
                                  "borrowerName": "%s"
                                }
                                """.formatted(bookId, borrowerName)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return readId(response);
    }

    private long readId(String response) throws Exception {
        JsonNode json = objectMapper.readTree(response);
        return json.get("id").asLong();
    }
}
