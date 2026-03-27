package com.taskmanager.repository.users;

import com.taskmanager.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<Users, Long> {
    // Проверка на существования данного email в базе
    boolean existsByEmail(String email);
    Optional<Users> findUsersByEmail(String email);
    List<Users> findAllByRole(String role);
    List<Users> findByFirstNameContainingAndLastNameContaining(String name, String lastName);
}
