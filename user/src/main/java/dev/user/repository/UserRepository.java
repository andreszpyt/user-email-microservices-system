package dev.user.repository;

import dev.user.domain.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<UserModel, UUID> {

    UserModel findByUsername(String username);
    UserModel findByEmail(String email);
    UserModel findByUsernameOrEmail(String username, String email);
}
