package com.example.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.backend.model.Appointment;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
	// ✅ Find appointment by transaction ID
    Optional<Appointment> findByTransactionId(String transactionId);
}


