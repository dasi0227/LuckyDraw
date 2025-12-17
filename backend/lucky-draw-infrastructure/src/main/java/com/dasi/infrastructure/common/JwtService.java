package com.dasi.infrastructure.common;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.dasi.properties.JwtProperties;
import com.dasi.domain.user.service.token.IJwtService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Service
public class JwtService implements IJwtService {

    @Resource
    private JwtProperties jwtProperties;

    public String generateToken(String userId) {
        long now = System.currentTimeMillis();
        Algorithm algorithm = Algorithm.HMAC256(jwtProperties.getSecret());
        return JWT.create()
                .withIssuer(jwtProperties.getIssuer())
                .withIssuedAt(new Date(now))
                .withExpiresAt(new Date(now + jwtProperties.getTtl()))
                .withClaim("userId", userId)
                .sign(algorithm);
    }

    public String parseUserId(String token) {
        Algorithm algorithm = Algorithm.HMAC256(jwtProperties.getSecret());
        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer(jwtProperties.getIssuer())
                .build();
        DecodedJWT decodedJWT = verifier.verify(token);
        return decodedJWT.getClaim("userId").asString();
    }

}
