package dev.user.dto;

import dev.user.domain.UserModel;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserModel toDomain(UserRequest request) {
        return UserModel.builder()
                .username(request.username())
                .email(request.email())
                .password(request.password())
                .build();
    }

    public UserResponse toResponse(UserModel model) {
        return UserResponse.builder()
                .id(model.getId())
                .username(model.getUsername())
                .email(model.getEmail())
                .build();
    }

}
