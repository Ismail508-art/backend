package com.example.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.backend.model.Order;
import com.example.backend.repository.OrderRepository;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = {"http://localhost:5173", "https://myqmulticare.com"}) // localhost for dev, live domain for production
public class OrderController {

    private final OrderRepository orderRepository;

    public OrderController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // ✅ CREATE order with SECRET_KEY validation
    @PostMapping
    public ResponseEntity<Order> createOrder(
            @RequestBody Order order,
            @RequestHeader(value = "X-SECRET-KEY", required = true) String secretKey) {

        final String MY_SECRET_KEY = "MYCLINIC2026"; // must match VITE_SECRET_KEY in frontend

        if (!MY_SECRET_KEY.equals(secretKey)) {
            return ResponseEntity.status(403).build(); // Forbidden if secret key is invalid
        }

        order.setPaymentStatus("PAID"); // or "PENDING" if you want manual confirmation
        Order saved = orderRepository.save(order);
        return ResponseEntity.ok(saved);
    }

    // ✅ GET all orders (for admin)
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderRepository.findAll());
    }

    // ✅ GET single order by ID
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return ResponseEntity.ok(order);
    }
}
