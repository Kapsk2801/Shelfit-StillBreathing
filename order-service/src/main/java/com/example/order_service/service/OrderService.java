package com.example.order_service.service;

import com.example.order_service.client.BookClient;
import com.example.order_service.client.UserClient;
import com.example.order_service.dto.BookDTO;
import com.example.order_service.dto.UserDTO;
import com.example.order_service.model.Order;
import com.example.order_service.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    // 🔗 Feign clients for communication with other services
    @Autowired
    private BookClient bookClient;

    @Autowired
    private UserClient userClient;

    public Order placeOrder(Order order) {
        // Fetch book details from book-service
        BookDTO book = bookClient.getBookById(order.getBookId());

        // Fetch user details from user-service
        UserDTO user = userClient.getUserById(order.getUserId());

        if (book == null || user == null) {
            throw new RuntimeException("Book or User not found!");
        }

        // Calculate total price using book price
        order.setTotalPrice(book.getPrice() * order.getQuantity());
        order.setStatus("PLACED");

        return orderRepository.save(order);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    public void cancelOrder(Long id) {
        orderRepository.deleteById(id);
    }
}
