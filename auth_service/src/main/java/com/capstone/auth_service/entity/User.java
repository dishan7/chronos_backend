package com.capstone.auth_service.entity;

import com.capstone.auth_service.enums.ROLE;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.management.relation.Role;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "username"),
                @UniqueConstraint(columnNames = "email")
        }
)
public class User extends BaseEntity{

    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 20)
    @Column(nullable = false, length = 20)
    private String username;

    @NotBlank(message = "Email cannot be blank")
    @Email
    @Column(nullable = false)
    private String email;

    @NotBlank
    @Size(min = 8)
    @Column(nullable = false)
    private String password;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ROLE role;

    private boolean isEnabled;
}
