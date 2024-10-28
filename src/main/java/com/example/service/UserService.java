package com.example.service;

import com.example.configuration.JwtConfiguration;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import com.example.controller.user.UserDto;
import com.example.entities.User;
import com.example.repository.UserRepository;
import jakarta.inject.Singleton;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import io.micronaut.transaction.annotation.Transactional;

@Slf4j
@Singleton
public class UserService {
    private static final long EXPIRATION_TIME = 864_000_000;

    private final SecretKey secretKey;
    private final UserRepository userRepository;

    @Inject
    public UserService(UserRepository userRepository,
                       JwtConfiguration jwtConfig) {
        this.userRepository = userRepository;
        byte[] decodedKey = Base64.getDecoder().decode(jwtConfig.getSecret());
        this.secretKey = Keys.hmacShaKeyFor(decodedKey);
    }

    @Transactional
    public Optional<UserError> createNewUser(UserDto dto) {
        if (dto.getUserName() == null || dto.getUserName().isEmpty()) {
            return Optional.of(UserError.BAD_REQUEST);
        }

        if (dto.getPassword() == null || dto.getPassword().isEmpty()) {
            return Optional.of(UserError.BAD_REQUEST);
        }

        User user = new User();
        user.setUserName(dto.getUserName());
        user.setPassword(dto.getPassword());

        try{
            userRepository.save(user);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Unexpected exception trying to save new User", e);
            return Optional.of(UserError.UNEXPECTED_ERROR);
        }
    }

    @Transactional
    public String authenticateUser(String username, String password) {
        // Busca al usuario en la base de datos
        Optional<User> optionalUser = userRepository.findByUserName(username);
        if (!optionalUser.isPresent() || !password.equals(optionalUser.get().getPassword())) {
            throw new RuntimeException("Credenciales inválidas");
        }
        String token = generateToken(username);

        User user = optionalUser.get();
        user.setToken(token);
        try{
            userRepository.save(user);
            return token;
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception trying to save new User");
        }
    }

    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(secretKey, SignatureAlgorithm.HS512)
            .compact();
    }

    public enum UserError {
        BAD_REQUEST,
        NO_USER_EXIST,
        UNEXPECTED_ERROR
    }
}
