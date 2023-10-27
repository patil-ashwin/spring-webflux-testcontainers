package com.cognizant.testcontainers.repo;

import com.cognizant.testcontainers.entity.Book;
import com.cognizant.testcontainers.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class BookRepositoryTest {

    @Container
    @ServiceConnection
    private static final MySQLContainer<?> CONTAINER = new MySQLContainer<>("mysql:latest");

    @Autowired
    private BookRepository bookRepository;

    @BeforeEach
    public void setUp() {
        CONTAINER.start();
    }

    @Test
    public void testCRUDOperations() throws Exception {
        // Create a book
        Book newBook = Book.builder().title("Head First Java").author("Bert Bates").build();

       Book book = bookRepository.save(newBook);
       assertThat(book).isNotNull();

        // Read the book
        List<Book> books = bookRepository.findAll();
        assertThat(books).hasSize(1);

        // Update the book
        Book updatedBook = books.get(0);
        updatedBook.setTitle("Head First Java");
        updatedBook.setAuthor("Bert Bates and Kathy Sierra");
        bookRepository.save(updatedBook);

        // Verify the update
        Book retrievedBook = bookRepository.findById(updatedBook.getId()).orElse(null);
        assertThat(retrievedBook).isNotNull();
        assertThat(retrievedBook.getTitle()).isEqualTo("Head First Java");
        assertThat(retrievedBook.getAuthor()).isEqualTo("Bert Bates and Kathy Sierra");

        // Delete the book
        bookRepository.deleteById(retrievedBook.getId());

        // Verify deletion
        Book deletedBook = bookRepository.findById(retrievedBook.getId()).orElse(null);
        assertThat(deletedBook).isNull();
    }
}
