package com.zorysa.finance.auth.security;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.zorysa.finance.shared.exception.UnauthorizedException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final JwtProperties properties;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public JwtService(JwtProperties properties, ObjectMapper objectMapper, Clock clock) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    public String createAccessToken(UUID userId, String email) {
        Instant now = clock.instant();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", userId.toString());
        payload.put("email", email);
        payload.put("iat", now.getEpochSecond());
        payload.put("exp", now.plusSeconds(properties.accessTokenExpirationSeconds()).getEpochSecond());
        return sign(payload);
    }

    public JwtSubject validateAccessToken(String token) {
        Map<String, Object> payload = parseAndVerify(token);
        long expiresAt = asLong(payload.get("exp"));
        if (clock.instant().getEpochSecond() >= expiresAt) {
            throw new UnauthorizedException("Token expirado");
        }
        return new JwtSubject(UUID.fromString(String.valueOf(payload.get("sub"))), String.valueOf(payload.get("email")));
    }

    public long accessTokenExpirationSeconds() {
        return properties.accessTokenExpirationSeconds();
    }

    private String sign(Map<String, Object> payload) {
        try {
            Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
            String encodedHeader = encodeJson(header);
            String encodedPayload = encodeJson(payload);
            String signingInput = encodedHeader + "." + encodedPayload;
            String signature = BASE64_URL_ENCODER.encodeToString(hmac(signingInput));
            return signingInput + "." + signature;
        } catch (Exception exception) {
            throw new IllegalStateException("Nao foi possivel gerar o JWT", exception);
        }
    }

    private Map<String, Object> parseAndVerify(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new UnauthorizedException("Token invalido");
            }
            String signingInput = parts[0] + "." + parts[1];
            byte[] expectedSignature = hmac(signingInput);
            byte[] actualSignature = BASE64_URL_DECODER.decode(parts[2]);
            if (!MessageDigest.isEqual(expectedSignature, actualSignature)) {
                throw new UnauthorizedException("Token invalido");
            }
            return objectMapper.readValue(BASE64_URL_DECODER.decode(parts[1]), MAP_TYPE);
        } catch (UnauthorizedException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new UnauthorizedException("Token invalido");
        }
    }

    private String encodeJson(Map<String, Object> value) throws Exception {
        return BASE64_URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(value));
    }

    private byte[] hmac(String signingInput) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(properties.secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return mac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8));
    }

    private long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }
}
