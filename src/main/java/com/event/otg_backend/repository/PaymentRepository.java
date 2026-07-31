package com.event.otg_backend.repository;

import com.event.otg_backend.models.Payment;
import com.event.otg_backend.models.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);

    long countByTicketTypeCodeAndStatus(String ticketTypeCode, PaymentStatus status);

    long countByTicketTypeCodeAndStatusAndCreatedAtAfter(String ticketTypeCode, PaymentStatus status, Instant threshold);

}
