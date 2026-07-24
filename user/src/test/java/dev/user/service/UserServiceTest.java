package dev.user.service;

import dev.user.controller.exceptions.CredentialAlreadyExistsException;
import dev.user.controller.exceptions.InvalidCredentialsException;
import dev.user.domain.UserModel;
import dev.user.dto.LoginRequest;
import dev.user.dto.UserMapper;
import dev.user.dto.UserRequest;
import dev.user.dto.UserResponse;
import dev.user.enums.Role;
import dev.user.producer.UserProducer;
import dev.user.repository.UserRepository;
import dev.user.security.TokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    @Mock
    private UserProducer userProducer;

    @InjectMocks
    private UserService userService;

    @Test
    void save_shouldPersistUserEncodePasswordAndPublishEmail() {
        UserRequest request = new UserRequest("Joao", "joao@email.com", "senha123");
        UUID userId = UUID.randomUUID();

        UserModel mappedUser = UserModel.builder()
                .username("Joao")
                .email("joao@email.com")
                .password("senha123")
                .build();

        UserModel savedUser = UserModel.builder()
                .id(userId)
                .username("Joao")
                .email("joao@email.com")
                .password("encoded-password")
                .role(Role.USER)
                .build();

        UserResponse expectedResponse = UserResponse.builder()
                .id(userId)
                .username("Joao")
                .email("joao@email.com")
                .build();

        when(userMapper.toDomain(request)).thenReturn(mappedUser);
        when(userRepository.findByUsername("Joao")).thenReturn(null);
        when(userRepository.findByEmail("joao@email.com")).thenReturn(null);
        when(passwordEncoder.encode("senha123")).thenReturn("encoded-password");
        when(userRepository.save(mappedUser)).thenReturn(savedUser);
        when(userMapper.toResponse(savedUser)).thenReturn(expectedResponse);

        UserResponse response = userService.save(request);

        assertEquals(expectedResponse, response);
        assertEquals(Role.USER, mappedUser.getRole());
        assertEquals("encoded-password", mappedUser.getPassword());
        verify(userProducer).publishEmailMessage(savedUser);
    }

    @Test
    void save_shouldThrowWhenUsernameAlreadyExists() {
        UserRequest request = new UserRequest("Joao", "joao@email.com", "senha123");
        UserModel mappedUser = UserModel.builder()
                .username("Joao")
                .email("joao@email.com")
                .password("senha123")
                .build();
        UserModel existingUser = UserModel.builder().username("Joao").build();

        when(userMapper.toDomain(request)).thenReturn(mappedUser);
        when(userRepository.findByUsername("Joao")).thenReturn(existingUser);

        CredentialAlreadyExistsException exception = assertThrows(
                CredentialAlreadyExistsException.class,
                () -> userService.save(request)
        );

        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository, never()).save(any());
        verify(userProducer, never()).publishEmailMessage(any());
    }

    @Test
    void save_shouldThrowWhenEmailAlreadyExists() {
        UserRequest request = new UserRequest("Joao", "joao@email.com", "senha123");
        UserModel mappedUser = UserModel.builder()
                .username("Joao")
                .email("joao@email.com")
                .password("senha123")
                .build();
        UserModel existingUser = UserModel.builder().email("joao@email.com").build();

        when(userMapper.toDomain(request)).thenReturn(mappedUser);
        when(userRepository.findByUsername("Joao")).thenReturn(null);
        when(userRepository.findByEmail("joao@email.com")).thenReturn(existingUser);

        CredentialAlreadyExistsException exception = assertThrows(
                CredentialAlreadyExistsException.class,
                () -> userService.save(request)
        );

        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository, never()).save(any());
        verify(userProducer, never()).publishEmailMessage(any());
    }

    @Test
    void login_shouldReturnTokenWhenCredentialsAreValid() {
        LoginRequest request = new LoginRequest("joao@email.com", "senha123");
        UserModel user = UserModel.builder()
                .email("joao@email.com")
                .password("encoded-password")
                .role(Role.USER)
                .build();

        when(userRepository.findByUsernameOrEmail("joao@email.com", "joao@email.com")).thenReturn(user);
        when(passwordEncoder.matches("senha123", "encoded-password")).thenReturn(true);
        when(tokenService.generateToken(user)).thenReturn("jwt-token");

        String token = userService.login(request);

        assertEquals("jwt-token", token);
        verify(tokenService).generateToken(user);
    }

    @Test
    void login_shouldThrowWhenUserIsNotFound() {
        LoginRequest request = new LoginRequest("unknown", "senha123");

        when(userRepository.findByUsernameOrEmail("unknown", "unknown")).thenReturn(null);

        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> userService.login(request)
        );

        assertEquals("Invalid credentials", exception.getMessage());
        verify(tokenService, never()).generateToken(any());
    }

    @Test
    void login_shouldThrowWhenPasswordDoesNotMatch() {
        LoginRequest request = new LoginRequest("Joao", "wrong-password");
        UserModel user = UserModel.builder()
                .username("Joao")
                .password("encoded-password")
                .build();

        when(userRepository.findByUsernameOrEmail("Joao", "Joao")).thenReturn(user);
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> userService.login(request)
        );

        assertEquals("Invalid credentials", exception.getMessage());
        verify(tokenService, never()).generateToken(any());
    }
}
