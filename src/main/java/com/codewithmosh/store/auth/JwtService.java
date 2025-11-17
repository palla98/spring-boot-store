package com.codewithmosh.store.auth;

import com.codewithmosh.store.users.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@AllArgsConstructor
@Service
public class JwtService {

    private final JwtConfig jwtConfig;

    // ####### CREATE TOKEN #########
    private Jwt generateToken(User user, long tokenExpiration) {

        //in pratica sto assemblando il payload del token(tutto quello che può autenticare lo user)
        var claims = Jwts.claims()
                        .subject(user.getId().toString())
                        .add("email", user.getEmail())
                        .add("name", user.getName())
                        .add("role",  user.getRole())
                        .issuedAt(new Date())
                        .expiration(new Date(System.currentTimeMillis() + (1000 * tokenExpiration)))
                        .build();

        return new Jwt(claims, jwtConfig.getSecretKey());
    }

    // ####### GENERATE ACCESS TOKEN #########
    public Jwt generateAccessToken(User user){
        return generateToken(user, jwtConfig.getAccessTokenExpiration());
    }

    // ####### GENERATE REFRESH TOKEN #########
    public Jwt generateRefreshToken(User user){
        return generateToken(user, jwtConfig.getRefreshTokenExpiration());
    }

    public Jwt parseToken(String token) {
        try {
            var claims = getClaims(token);
            return new  Jwt(claims, jwtConfig.getSecretKey());
        }
        catch (JwtException e) {
            return null;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(jwtConfig.getSecretKey()) // va a verificare con la segret key
                .build()
                .parseSignedClaims(token) //gli passa il token
                .getPayload();
    }



}
