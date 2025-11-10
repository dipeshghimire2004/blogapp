package org.blogapp.dg_blogapp.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TokenRefreshResult {

    private String accessToken;
    private String refreshToken;
    private String message;
}
