package com.appcoiffure.config;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.appcoiffure.coiffeuse.Coiffeuse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();
    private static final TypeReference<Map<String, Object>> CLAIMS_TYPE = new TypeReference<>() {};

    private final byte[] secret;
    private final long expirationMs;
    private final ObjectMapper objectMapper;

    public JwtService(
            @Value("${app.security.jwt.secret}") String secret,
            @Value("${app.security.jwt.expiration-ms}") long expirationMs,
            ObjectMapper objectMapper
    ) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationMs = expirationMs;
        this.objectMapper = objectMapper;
    }

    public String generateToken(Coiffeuse coiffeuse) {
        long now = Instant.now().toEpochMilli();

        Map<String, Object> header = Map.of(
                "alg", "HS256",
                "typ", "JWT"
        );
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("sub", coiffeuse.getEmail());
        claims.put("coiffeuseId", coiffeuse.getId());
        claims.put("nom", coiffeuse.getNom());
        claims.put("iat", now);
        claims.put("exp", now + expirationMs);

        String unsignedToken = encodeJson(header) + "." + encodeJson(claims);

        return unsignedToken + "." + sign(unsignedToken);
    }

    public String extractEmail(String token) {
        Object subject = extractClaims(token).get("sub");
        return subject instanceof String email ? email : null;
    }

    public boolean isTokenValid(String token) {
        String email = extractEmail(token);
        return email != null
                && hasValidSignature(token)
                && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        Object expiration = extractClaims(token).get("exp");

        if (expiration instanceof Number expirationNumber) {
            return expirationNumber.longValue() < Instant.now().toEpochMilli();
        }

        return true;
    }

    private boolean hasValidSignature(String token) {
        String[] parts = token.split("\\.");

        if (parts.length != 3) {
            return false;
        }

        String expectedSignature = sign(parts[0] + "." + parts[1]);
        return MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                parts[2].getBytes(StandardCharsets.UTF_8)
        );
    }

    private Map<String, Object> extractClaims(String token) {
        try {
            String[] parts = token.split("\\.");

            if (parts.length != 3) {
                return Map.of();
            }

            byte[] payload = BASE64_URL_DECODER.decode(parts[1]);
            return objectMapper.readValue(payload, CLAIMS_TYPE);
        } catch (Exception exception) {
            return Map.of();
        }
    }

    private String encodeJson(Map<String, Object> value) {
        try {
            return BASE64_URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(value));
        } catch (Exception exception) {
            throw new IllegalStateException("Impossible de creer le jeton JWT", exception);
        }
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return BASE64_URL_ENCODER.encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Impossible de signer le jeton JWT", exception);
        }
    }
}
