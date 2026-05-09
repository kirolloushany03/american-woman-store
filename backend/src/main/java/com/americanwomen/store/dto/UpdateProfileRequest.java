package com.americanwomen.store.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @Email(message = "Email must be valid")
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String city;
    private String address1;
    private String address2;
}

