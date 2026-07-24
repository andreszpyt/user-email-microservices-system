package dev.user.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import dev.user.domain.UserModel;
import dev.user.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @InjectMocks
    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tokenService, "secret", "test-secret-key-for-jwt-signing");
        ReflectionTestUtils.setField(tokenService, "expirationHours", 2);
    }

    @Test
    void generateToken_shouldCreateValidJwtWithSubjectAndRole() {
        UserModel user = UserModel.builder()
                .email("joao@email.com")
                .role(Role.USER)
                .build();

        String token = tokenService.generateToken(user);

        assertNotNull(token);
        assertEquals("joao@email.com", tokenService.validateToken(token));

        DecodedJWT decodedJWT = tokenService.getDecodedToken(token);
        assertNotNull(decodedJWT);
        assertEquals("auth-api", decodedJWT.getIssuer());
        assertEquals("joao@email.com", decodedJWT.getSubject());
        assertEquals("USER", decodedJWT.getClaim("role").asString());
    }

    @Test
    void validateToken_shouldReturnEmptyStringForInvalidToken() {
        assertEquals("", tokenService.validateToken("invalid.token.value"));
    }

    @Test
    void getDecodedToken_shouldReturnNullForInvalidToken() {
        assertNull(tokenService.getDecodedToken("invalid.token.value"));
    }

    @Test
    void generateToken_shouldIncludeAdminRoleClaim() {
        UserModel user = UserModel.builder()
                .email("admin@email.com")
                .role(Role.ADMIN)
                .build();

        DecodedJWT decodedJWT = tokenService.getDecodedToken(tokenService.generateToken(user));

        assertNotNull(decodedJWT);
        assertEquals("ADMIN", decodedJWT.getClaim("role").asString());
        assertNotNull(decodedJWT.getExpiresAtAsInstant());
        assertTrue(decodedJWT.getExpiresAtAsInstant().isAfter(java.time.Instant.now().minusSeconds(1)));
    }
}
