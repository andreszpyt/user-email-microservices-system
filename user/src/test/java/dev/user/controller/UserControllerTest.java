package dev.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.user.controller.exceptions.CredentialAlreadyExistsException;
import dev.user.controller.exceptions.GlobalExceptionHandler;
import dev.user.controller.exceptions.InvalidCredentialsException;
import dev.user.dto.LoginRequest;
import dev.user.dto.UserRequest;
import dev.user.dto.UserResponse;
import dev.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new UserController(userService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(
                        new StringHttpMessageConverter(StandardCharsets.UTF_8),
                        new MappingJackson2HttpMessageConverter(objectMapper)
                )
                .build();
    }

    @Test
    void register_shouldReturnCreatedWithUserResponse() throws Exception {
        UserRequest request = new UserRequest("Joao", "joao@email.com", "senha123");
        UUID userId = UUID.randomUUID();
        UserResponse response = UserResponse.builder()
                .id(userId)
                .username("Joao")
                .email("joao@email.com")
                .build();

        when(userService.save(any(UserRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("Joao"))
                .andExpect(jsonPath("$.email").value("joao@email.com"));
    }

    @Test
    void register_shouldReturnConflictWhenCredentialsAlreadyExist() throws Exception {
        UserRequest request = new UserRequest("Joao", "joao@email.com", "senha123");

        when(userService.save(any(UserRequest.class)))
                .thenThrow(new CredentialAlreadyExistsException("Username already exists"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().string("Username already exists"));
    }

    @Test
    void register_shouldReturnBadRequestWhenPayloadIsInvalid() throws Exception {
        UserRequest request = new UserRequest("", "not-an-email", "");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_shouldReturnAcceptedWithToken() throws Exception {
        LoginRequest request = new LoginRequest("joao@email.com", "senha123");

        when(userService.login(any(LoginRequest.class))).thenReturn("jwt-token");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(content().string("jwt-token"));
    }

    @Test
    void login_shouldReturnUnauthorizedWhenCredentialsAreInvalid() throws Exception {
        LoginRequest request = new LoginRequest("joao@email.com", "wrong");

        when(userService.login(any(LoginRequest.class)))
                .thenThrow(new InvalidCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid credentials"));
    }
}
