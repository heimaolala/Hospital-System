package com.hospital.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long userId;
    private String name;
    private Integer role;
    private boolean mustChangePwd;

    public static LoginResponse of(String accessToken, String refreshToken, Long userId, String name, Integer role, boolean mustChangePwd) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(userId)
                .name(name)
                .role(role)
                .mustChangePwd(mustChangePwd)
                .build();
    }
}
