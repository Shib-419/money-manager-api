package com.shiv.MoneyManager.dataTransferObjects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthDTO {
    private String email;
    private String password;
    private String token;
}
