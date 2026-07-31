package com.event.otg_backend.repository;

import com.event.otg_backend.models.Ticket;
import com.event.otg_backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TicketRepository  extends JpaRepository<Ticket, Integer> {

    boolean existsByTicketCode(String ticketCode);

    Optional<Ticket> findByUser(User user);
}
