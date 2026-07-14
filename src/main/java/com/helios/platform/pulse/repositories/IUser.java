package com.helios.platform.pulse.repositories;

import com.helios.platform.pulse.entities.Role;
import com.helios.platform.pulse.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface IUser extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByEmail(String email);
    long countByRole(Role role);
    List<UserEntity> findByRoleNot(Role role);
}
