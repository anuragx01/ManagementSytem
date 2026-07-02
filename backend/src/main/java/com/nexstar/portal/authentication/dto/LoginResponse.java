package com.nexstar.portal.authentication.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Set;
import java.util.UUID;

@Getter
@Builder
public class LoginResponse {

    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private String profilePictureUrl;
    private Set<String> roles;
    private Set<String> permissions;
    private String accessToken;
    private String refreshToken;
    private long accessTokenExpiresIn;
    private String tokenType;
}
