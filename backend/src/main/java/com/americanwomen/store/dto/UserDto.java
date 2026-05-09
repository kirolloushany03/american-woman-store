package com.americanwomen.store.dto;

import com.americanwomen.store.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String city;
    private String address1;
    private String address2;
    private User.Role role;

    public static UserDto from(User user) {
        return new UserDto(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getPhone(),
            user.getCity(),
            user.getAddress1(),
            user.getAddress2(),
            user.getRole()
        );
    }
}

