package com.chronos.job_scheduler.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;

import java.util.Date;

public class TokenUtil {

    public static String generateToken(String subject, String role){
        return Jwts.builder()
                .subject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 5 * 60 * 1000))
                .claim("roles", "ROLE_" + role)
                .signWith(io.jsonwebtoken.SignatureAlgorithm.HS256, "secretKeyPersonalTestingTokensecretKeyPersonalTestingTokensecretKeyPersonalTestingTokensecretKeyPersonalTestingToken")
                .compact();
    }

    public static Claims validateSignedToken(String authorizationHeader){
        try{
            Claims body = Jwts.parser()
                    .setSigningKey("secretKeyPersonalTestingTokensecretKeyPersonalTestingTokensecretKeyPersonalTestingTokensecretKeyPersonalTestingToken")
                    .build()
                    .parseClaimsJws(authorizationHeader)
                    .getBody();
            System.out.println("Claims" + body);
            return body;
        }
        catch(io.jsonwebtoken.ExpiredJwtException e) {
            System.out.println("JWT token is expired: " + e.getMessage());
            return null;
        }
        catch(io.jsonwebtoken.MalformedJwtException e) {
            System.out.println("Malformed JWT token: " + e.getMessage());
            return null;
        }
        catch(io.jsonwebtoken.UnsupportedJwtException e) {
            System.out.println("Unsupported JWT token: " + e.getMessage());
            return null;
        }
        catch(SignatureException e) {
            System.out.println("JWT signature does not match locally computed signature: " + e.getMessage());
            return null;
        }
        catch(IllegalArgumentException e) {
            System.out.println("JWT claims string is empty: " + e.getMessage());
            return null;
        }
    }
}
