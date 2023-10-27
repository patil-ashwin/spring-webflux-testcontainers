package com.cognizant.testcontainers.integration;

import com.cognizant.testcontainers.entity.Book;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestMethodOrder(OrderAnnotation.class)
@Testcontainers
public class BookAPIIntegrationTest {

    @Container
    @ServiceConnection // this annotation is replacement for @DynamicPropertySource with default values
    private static final MySQLContainer<?> CONTAINER = new MySQLContainer<>("mysql:latest");


    //@DynamicPropertySource
    static void configureTestProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> CONTAINER.getJdbcUrl());
        registry.add("spring.datasource.username", () -> CONTAINER.getUsername());
        registry.add("spring.datasource.password", () -> CONTAINER.getPassword());
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");
    }

    @Autowired
    private WebTestClient webTestClient;

    @BeforeAll
    public static void setUp() {
        CONTAINER.start();
    }

    @AfterAll
    public static void tearDown() {
        CONTAINER.stop();
    }


    @Test
    @DisplayName("Add a new book ")
    @Order(1)
    void saveBook() {
        // Create a book
        Book newBook = Book.builder().title("Head First Java").author("Bert Bates").build();

        webTestClient.post()
                .uri("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(newBook), Book.class)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON);
    }

    @Test
    @DisplayName("Read All Books")
    @Order(2)
    void readAllBooks() {

        // Read all the book
        webTestClient.get()
                .uri("/api/books")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Book.class).hasSize(1);
    }


    @Test
    @DisplayName("Update an Existing Book")
    @Order(3)
    void updateBook() {
        // Update the book
        webTestClient.put()
                .uri("/api/books/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(
                        Book.builder().title("Head First Java").author("Bert Bates and Kathy Sierra").build()), Book.class)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON);
    }


    @Test
    @DisplayName("Get a Specific Book by ID")
    @Order(4)
    void readBookById() {
        // Verify the update
        webTestClient.get()
                .uri("/api/books/1")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Book.class)
                .value(book -> {
                    assertThat(book.getTitle()).isEqualTo("Head First Java");
                    assertThat(book.getAuthor()).isEqualTo("Bert Bates and Kathy Sierra");
                });
    }

    @Test
    @DisplayName("Delete a Book by ID")
    @Order(5)
    void deleteBookById() {

        // Delete the book
        webTestClient.delete()
                .uri("/api/books/{id}", 1)
                .exchange()
                .expectStatus().isNoContent();
    }

}
