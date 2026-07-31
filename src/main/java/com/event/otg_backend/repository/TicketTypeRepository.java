package com.event.otg_backend.repository;

import com.event.otg_backend.models.TicketType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TicketTypeRepository  extends JpaRepository<TicketType, Long> {

    Optional<TicketType> findByCode(String code);

    // Used inside the seat-reservation transaction: locks this ticket_types row (SELECT ... FOR UPDATE)
    // so two concurrent orders can't both grab the last seat.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from TicketType t where t.code = :code")
    Optional<TicketType> findByCodeForUpdate(@Param("code") String code);

}
