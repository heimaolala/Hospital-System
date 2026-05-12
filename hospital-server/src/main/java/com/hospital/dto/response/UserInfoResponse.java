package com.hospital.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {
    private Long id;
    private String idCard;
    private String name;
    private Integer age;
    private Integer gender;
    private String address;
    private String phone;
    private Integer role;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;

    private String hospital;
    private String department;
    private String title;
    private String specialty;
}
