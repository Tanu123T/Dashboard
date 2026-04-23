package com.ceodashboard.backend.entity;

import com.ceodashboard.backend.enums.Role;
import jakarta.persistence.*;
import lombok.*;

@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;
}