package com.wooyoung.mes.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginTokenResponse {

    private String accessToken;
    private String refreshToken;
}