package com.event.otg_backend.repository;

import com.event.otg_backend.models.Payment;
import com.event.otg_backend.models.PaymentStatus;
import com.event.otg_backend.models.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);

    long countByTicketTypeCodeAndStatus(String ticketTypeCode, PaymentStatus status);

    long countByTicketTypeCodeAndStatusAndCreatedAtAfter(String ticketTypeCode, PaymentStatus status, Instant threshold);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Payment p where p.razorpayOrderId = :orderId")
    Optional<Payment> findByRazorpayOrderIdForUpdate(@Param("orderId") String orderId);

    Optional<Payment> findFirstByUserAndStatusAndCreatedAtAfterOrderByCreatedAtDesc(User user, PaymentStatus status, Instant threshold);

}
