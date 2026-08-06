package com.event.otg_backend.repository;

import com.event.otg_backend.models.Ticket;
import com.event.otg_backend.models.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TicketRepository  extends JpaRepository<Ticket, Integer> {

    boolean existsByTicketCode(String ticketCode);

    Optional<Ticket> findByUser(User user);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Ticket t where t.id = :id")
    Optional<Ticket> findByIdForUpdate(@Param("id") int id);

    // Confirmed tickets whose email never went out and that are older than the given cutoff.
    List<Ticket> findByEmailSentFalseAndTimestampBefore(Instant threshold);

    long countByTicketTypeCode(String ticketTypeCode);

    @Query("select coalesce(sum(t.price), 0) from Ticket t")
    Double sumAllPrices();
}
