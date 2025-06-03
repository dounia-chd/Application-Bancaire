package com.ebanca.repository;

import com.ebanca.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findByUsernameOrEmail(String username, String email);
    Page<User> findByRolesContaining(String role, Pageable pageable);
    Page<User> findByUsernameContainingOrEmailContainingOrFirstNameContainingOrLastNameContaining(
        String username, String email, String firstName, String lastName, Pageable pageable);
    Optional<User> findByResetPasswordToken(String token);
    
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.username = :username")
    Optional<User> findByUsernameWithRoles(String username);
    
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.accounts WHERE u.id = :id")
    Optional<User> findByIdWithAccounts(Long id);
    
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.loans WHERE u.id = :id")
    Optional<User> findByIdWithLoans(Long id);
} 