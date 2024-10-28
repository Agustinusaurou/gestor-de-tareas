package com.example.configuration;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("micronaut.security.token.jwt.signatures.secret.generator")
public class JwtConfiguration {
    private String secret;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
