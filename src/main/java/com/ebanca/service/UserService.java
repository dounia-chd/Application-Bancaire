package com.ebanca.service;


import com.ebanca.model.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;




import java.util.Optional;

public interface UserService extends GenericService<User, Long>, UserDetailsService {
    User createUser(User user);
    User updateUser(Long id, User user);
    void deleteUser(Long id);
    Optional<User> findById(Long id);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findByUsernameOrEmail(String username, String email);
    Page<User> findAll(Pageable pageable);
    Page<User> findByRole(String role, Pageable pageable);
    Page<User> searchUsers(String query, Pageable pageable);
    void enableUser(Long id);
    void disableUser(Long id);
    void changePassword(Long id, String oldPassword, String newPassword);
    void sendPasswordResetEmail(String email);
    void resetPassword(String token, String newPassword);
    void addRole(Long userId, String role);
    void removeRole(Long userId, String role);
    void lockUser(Long userId);
    void unlockUser(Long userId);
    User getCurrentUser();
    User register(String username, String email, String password);
} 