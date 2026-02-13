package com.example.backend.controller;

import java.util.List;
import java.io.ByteArrayOutputStream;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.backend.model.Appointment;
import com.example.backend.repository.AppointmentRepository;

import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "https://react-frontend-theta-two.vercel.app") // your live domain

public class AppointmentController {

    private final AppointmentRepository appointmentRepository;

    public AppointmentController(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    // ✅ CREATE appointment (Payment PENDING)
    @PostMapping
    public ResponseEntity<Appointment> createAppointment(@RequestBody Appointment appointment) {
        appointment.setPaymentStatus("PENDING");
        Appointment saved = appointmentRepository.save(appointment);
        return ResponseEntity.ok(saved);
    }

    // ✅ GET all appointments
    @GetMapping
    public ResponseEntity<List<Appointment>> getAllAppointments() {
        return ResponseEntity.ok(appointmentRepository.findAll());
    }

    // ✅ UPDATE payment status with SECRET KEY
    @PatchMapping("/{id}")
    public ResponseEntity<Appointment> updatePaymentStatus(
            @PathVariable Long id,
            @RequestBody PaymentUpdateRequest request,
            @RequestHeader(value = "X-SECRET-KEY", required = true) String secretKey) {

        final String MY_SECRET_KEY = "MYCLINIC2026"; // change to a strong secret
        if (!MY_SECRET_KEY.equals(secretKey)) {
            return ResponseEntity.status(403).build();
        }

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!"PENDING".equals(appointment.getPaymentStatus())) {
            throw new RuntimeException("Payment already processed or invalid status");
        }

     // ✅ Check if transactionId is already used
        if (request.getTransactionId() != null && !request.getTransactionId().isEmpty()) {
            appointmentRepository.findByTransactionId(request.getTransactionId())
                .ifPresent(a -> {
                    throw new RuntimeException("Transaction ID already used for another appointment");
                });
        }

        appointment.setPaymentStatus("CONFIRMED");
        appointment.setTransactionId(request.getTransactionId());

        Appointment updated = appointmentRepository.save(appointment);
        return ResponseEntity.ok(updated);
    }

    // ✅ GENERATE PDF SLIP (ONLY IF PAYMENT CONFIRMED)
    @GetMapping("/slip/{id}")
    public ResponseEntity<byte[]> getSlip(@PathVariable Long id) throws Exception {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!"CONFIRMED".equals(appointment.getPaymentStatus())) {
            throw new RuntimeException("Payment not completed yet");
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("Appointment Confirmation").setBold().setFontSize(18));
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Appointment ID: " + appointment.getId()));
        document.add(new Paragraph("Name: " + appointment.getName()));
        document.add(new Paragraph("Email: " + appointment.getEmail()));
        document.add(new Paragraph("Contact: " + appointment.getContact()));
        document.add(new Paragraph("Treatment: " + appointment.getTreatment()));
        document.add(new Paragraph("Date: " + appointment.getDate()));
        document.add(new Paragraph("Time: " + appointment.getTime()));
        document.add(new Paragraph("Amount Paid: ₹" + appointment.getPrice()));
        document.add(new Paragraph("Status: Payment Confirmed"));

        document.close();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData(
                "attachment",
                "appointment_" + appointment.getId() + ".pdf"
        );

        return ResponseEntity.ok().headers(headers).body(out.toByteArray());
    }

    // ✅ Request body for payment update
    public static class PaymentUpdateRequest {
        private String transactionId;
        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    }
}
