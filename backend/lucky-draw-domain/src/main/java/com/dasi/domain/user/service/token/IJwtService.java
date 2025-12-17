package com.dasi.domain.user.service.token;

public interface IJwtService {

    String generateToken(String userId);

}
