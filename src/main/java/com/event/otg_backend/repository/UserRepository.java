package com.event.otg_backend.repository;

import com.event.otg_backend.dtos.admin.AdminUserRowDto;
import com.event.otg_backend.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query(value = """
            select new com.event.otg_backend.dtos.admin.AdminUserRowDto(
                u.id, u.firstName, u.lastName, u.email, u.phoneNumber,
                u.cityState, u.collegeOrOrg, u.paymentStatus,
                t.id, t.emailSent, u.createdAt
            )
            from User u
            left join Ticket t on t.user = u
            where (:q is null
                   or lower(u.email) like :q
                   or lower(concat(coalesce(u.firstName, ''), ' ', coalesce(u.lastName, ''))) like :q)
            order by u.createdAt desc
            """,
            countQuery = """
            select count(u)
            from User u
            where (:q is null
                   or lower(u.email) like :q
                   or lower(concat(coalesce(u.firstName, ''), ' ', coalesce(u.lastName, ''))) like :q)
            """)
    Page<AdminUserRowDto> findAdminRows(@Param("q") String q, Pageable pageable);
}
