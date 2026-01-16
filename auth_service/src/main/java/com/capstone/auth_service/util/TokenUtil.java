package com.capstone.auth_service.util;

import com.capstone.auth_service.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;

import java.util.Date;

public class TokenUtil {

    public static String generateToken(User user, String role){
        return Jwts.builder()
                    .subject(user.getUsername())
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + 8 * 60 * 60 * 1000))
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
