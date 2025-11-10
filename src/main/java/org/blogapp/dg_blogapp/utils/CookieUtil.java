package org.blogapp.dg_blogapp.utils;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;

@Service
public class CookieUtil {

    private static final String ACCESS_TOKEN_COOKIE = "accessToken";
    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";


    @Value("${jwt.access-token-expiration}")
    private int ACCESS_TOKEN_MAX_AGE;

    @Value("${jwt.refresh-token-expiration}")
    private int REFRESH_TOKEN_MAX_AGE;

    private Cookie createSecureCookie(String name, String value, int maxAge){

        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        cookie.setAttribute("SameSite", "Strict"); // CSRF protection

        return cookie;
    }

   public void setAccessTokenCookie(HttpServletResponse response, String accessToken){
       int maxAgeSeconds = (int) (ACCESS_TOKEN_MAX_AGE / 1000);
        Cookie cookie = createSecureCookie(ACCESS_TOKEN_COOKIE, accessToken, maxAgeSeconds);
        response.addCookie(cookie);

        // Manual set-cookie header for better samesite support
       response.addHeader("Set-Cookie", String.format("%s=%s; path=/; HttpOnly; Secure; SameSite=Strict; Max-Age=%d", ACCESS_TOKEN_COOKIE, accessToken, maxAgeSeconds));
    }

   public void setRefreshTokenCookie(HttpServletResponse response, String refreshToken){
       int maxAgeSeconds = (int) (REFRESH_TOKEN_MAX_AGE / 1000);
       Cookie cookie = createSecureCookie(REFRESH_TOKEN_COOKIE, refreshToken, maxAgeSeconds);
       response.addCookie(cookie);

       //Manual set-cookie header for better same site fupport
       response.addHeader("Set-Cookie",
               String.format("%s=%s; path=/; HttpOnly; Secure; SameSite=Strict; Max-Age=%d", REFRESH_TOKEN_COOKIE, refreshToken, maxAgeSeconds));

   }


    public Optional<String> getCookieValue(HttpServletRequest request, String cookieName) {
        if(request.getCookies() == null){
            return Optional.empty();
        }

        return Arrays.stream(request.getCookies())
                .filter(cookie -> cookieName.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    public Optional<String> getAccessToken(HttpServletRequest request) {
        return getCookieValue(request, ACCESS_TOKEN_COOKIE);
    }

    public Optional<String> getRefreshToken(HttpServletRequest request) {
        return getCookieValue(request, REFRESH_TOKEN_COOKIE);
    }

    /**
     * Clear authentication cookies (logout)
     */
    public void clearAuthCookie(HttpServletResponse response) {
        Cookie accessCookie = new Cookie(ACCESS_TOKEN_COOKIE, null);
        accessCookie.setPath("/");
        accessCookie.setHttpOnly(true);
        accessCookie.setMaxAge(0);
        response.addCookie(accessCookie);

        Cookie refreshCookie = new Cookie(REFRESH_TOKEN_COOKIE, null);
        refreshCookie.setPath("/");
        refreshCookie.setHttpOnly(true);
        refreshCookie.setMaxAge(0);
        response.addCookie(refreshCookie);
    }
}
