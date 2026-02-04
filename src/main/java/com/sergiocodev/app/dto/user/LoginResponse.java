package com.sergiocodev.app.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private String refreshToken;
    private String type = "Bearer";
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String profilePicture;
    private Set<String> roles;
    private Set<String> permissions;

    public LoginResponse(String token, String refreshToken, Long id, String username, String email, String fullName,
            String profilePicture, Set<String> roles, Set<String> permissions) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.profilePicture = profilePicture;
        this.roles = roles;
        this.permissions = permissions;
    }
}
