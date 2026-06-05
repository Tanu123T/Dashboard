package com.ceodashboard.backend.hrms.repository;

import com.ceodashboard.backend.hrms.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ContactRepository extends JpaRepository<Contact, Long> {
    Optional<Contact> findFirstByRefTableIdOrderByIdDesc(Long refTableId);
}
