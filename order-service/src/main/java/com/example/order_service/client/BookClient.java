package com.example.order_service.client;

import com.example.order_service.dto.BookDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "book-service")
public interface BookClient {
    @GetMapping("/api/books/{id}")
    BookDTO getBookById(@PathVariable Long id);
}
