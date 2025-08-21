package com.shiv.MoneyManager.util;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Component
public class JWTUtil {

    private final Key key;
    private final long expirationTime;

    // Constructor me application.properties se value read ho rahi hai
    public JWTUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration}") long expirationTime) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationTime = expirationTime;
    }

    // Token se username extract karna
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Token se koi bhi specific claim nikalna
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Token banane ka method
    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username) // user ki info
                .issuedAt(new Date(System.currentTimeMillis())) // token kab bana
                .expiration(new Date(System.currentTimeMillis() + expirationTime)) // kab expire hoga
                .signWith(key) // secret key se sign karna
                .compact();
    }

    // Token verify karna valid hai ya nahi
    public boolean validateToken(String token, String username) {
        String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    // Token expire hua ya nahi
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Expiration date nikalna
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Saare claims nikalna
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) key) // secret key verify karna
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
