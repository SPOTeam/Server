package com.example.spot.web.dto.rsa;

import lombok.*;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDateTime;

@Getter
public class Rsa {

    @Getter
    @Builder
    @RequiredArgsConstructor
    public static class RSAKey {
        private final LocalDateTime createdAt;
        private final PrivateKey privateKey;
        private final PublicKey publicKey;
        private final String modulus;
        private final String exponent;
    }

    @Getter
    @Builder
    @RequiredArgsConstructor
    public static class RSAPublicKey {
        private final Long rsaId;
        private final String modulus;
        private final String exponent;
    }


}
