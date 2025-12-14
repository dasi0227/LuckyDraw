package com.dasi.infrastructure.common;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.dasi.domain.user.service.token.IJwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtService implements IJwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.ttl}")
    private Long ttl;

    public String generateToken(String userId) {
        long now = System.currentTimeMillis();
        Algorithm algorithm = Algorithm.HMAC256(secret);
        return JWT.create()
                .withIssuer(issuer)
                .withIssuedAt(new Date(now))
                .withExpiresAt(new Date(now + ttl))
                .withClaim("userId", userId)
                .sign(algorithm);
    }

    public String parseUserId(String token) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer(issuer)
                .build();
        DecodedJWT decodedJWT = verifier.verify(token);
        return decodedJWT.getClaim("userId").asString();
    }

}
