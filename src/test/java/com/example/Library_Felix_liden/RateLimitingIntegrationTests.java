package com.example.Library_Felix_liden;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "app.rate-limit.requests-per-minute=2")
@AutoConfigureMockMvc
class RateLimitingIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void rejectsRequestsThatExceedPerIpRateLimit() throws Exception {
        mockMvc.perform(get("/api/v1/books").header("Authorization", basicAuth()))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Rate-Limit-Limit", "2"));

        mockMvc.perform(get("/api/v1/books").header("Authorization", basicAuth()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/books").header("Authorization", basicAuth()))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.error").value("Rate limit exceeded"))
                .andExpect(jsonPath("$.details.retryAfterSeconds").exists());
    }

    private String basicAuth() {
        String credentials = "library-client:changeit";
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }
}
