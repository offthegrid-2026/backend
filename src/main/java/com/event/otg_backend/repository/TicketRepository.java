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

    // --- gate scanning ---

    Optional<Ticket> findByTicketCode(String ticketCode);

    Optional<Ticket> findByUserId(Long userId);

    // Row-locked variants: two phones scanning the same ticket at once must not both admit.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Ticket t where t.ticketCode = :code")
    Optional<Ticket> findByTicketCodeForUpdate(@Param("code") String code);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Ticket t where t.user.id = :userId")
    Optional<Ticket> findByUserIdForUpdate(@Param("userId") Long userId);

    // Confirmed tickets whose email never went out and that are older than the given cutoff.
    List<Ticket> findByEmailSentFalseAndTimestampBefore(Instant threshold);

    long countByTicketTypeCode(String ticketTypeCode);

    @Query("select coalesce(sum(t.price), 0) from Ticket t")
    Double sumAllPrices();
}
