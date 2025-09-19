package com.LDE.monFax_backend.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class RegisterDTO {
    private String username;
    private String email;
    private String password;
    private String numero;
    private LocalDate dateNaissance;
    private String filiere;
}