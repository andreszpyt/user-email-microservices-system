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
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final UserProducer userProducer;

    public UserService(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder, TokenService tokenService, UserProducer userProducer) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.userProducer = userProducer;
    }

    @Transactional
    public UserResponse save(UserRequest userRequest) {
        UserModel user = userMapper.toDomain(userRequest);
        user.setRole(Role.USER);

        if(userRepository.findByUsername(user.getUsername()) != null){
            throw new CredentialAlreadyExistsException("Username already exists");
        }

        if(userRepository.findByEmail(user.getEmail()) != null){
            throw new CredentialAlreadyExistsException("Email already exists");
        }

        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        UserModel savedUser = userRepository.save(user);
        userProducer.publishEmailMessage(savedUser);
        return userMapper.toResponse(savedUser);
    }

    public String login(LoginRequest loginRequest) {
        UserModel user = userRepository.findByUsernameOrEmail(loginRequest.login(), loginRequest.login());

        if(user == null || !passwordEncoder.matches(loginRequest.password(), user.getPassword())){
            throw new InvalidCredentialsException("Invalid credentials");
        }

        return tokenService.generateToken(user);
    }
}
