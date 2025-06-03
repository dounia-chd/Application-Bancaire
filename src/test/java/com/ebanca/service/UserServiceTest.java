package com.ebanca.service;

import com.ebanca.exception.BusinessException;
import com.ebanca.exception.ResourceNotFoundException;
import com.ebanca.model.User;
import com.ebanca.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;



import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;
    private User savedUser;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEnabled(1);

        savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername(user.getUsername());
        savedUser.setEmail(user.getEmail());
        savedUser.setPassword("encodedPassword");
        savedUser.setFirstName(user.getFirstName());
        savedUser.setLastName(user.getLastName());
        savedUser.setEnabled(1);
    }

    @Test
    void whenCreateUser_thenUserIsSaved() {
        when(userRepository.existsByUsername(user.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(user.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.createUser(user);

        assertNotNull(result);
        assertEquals(savedUser.getId(), result.getId());
        assertEquals(savedUser.getUsername(), result.getUsername());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void whenCreateUserWithExistingUsername_thenThrowException() {
        when(userRepository.existsByUsername(user.getUsername())).thenReturn(true);

        assertThrows(BusinessException.class, () -> userService.createUser(user));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void whenFindById_thenUserIsReturned() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(savedUser));

        Optional<User> result = userService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(savedUser.getId(), result.get().getId());
        assertEquals(savedUser.getUsername(), result.get().getUsername());
    }

    @Test
    void whenFindByIdWithInvalidId_thenThrowException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, 
            () -> userService.findById(999L).orElseThrow(() -> new ResourceNotFoundException("User not found")));
    }

    @Test
    void whenFindByUsername_thenUserIsReturned() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(savedUser));

        Optional<User> result = userService.findByUsername("testuser");

        assertTrue(result.isPresent());
        assertEquals(savedUser.getUsername(), result.get().getUsername());
    }

    @Test
    void whenFindByUsernameWithInvalidUsername_thenThrowException() {
        when(userRepository.findByUsername("invalid")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, 
            () -> userService.findByUsername("invalid").orElseThrow(() -> new ResourceNotFoundException("User not found")));
    }

    @Test
    void whenUpdateUser_thenUserIsUpdated() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(savedUser));
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User updatedUser = new User();
        updatedUser.setFirstName("Updated");
        updatedUser.setLastName("Name");

        User result = userService.updateUser(1L, updatedUser);

        assertNotNull(result);
        assertEquals("Updated", result.getFirstName());
        assertEquals("Name", result.getLastName());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void whenDeleteUser_thenUserIsDeleted() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteById(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void whenDeleteUserWithInvalidId_thenThrowException() {
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> userService.deleteById(999L));
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void whenEnableUser_thenUserIsEnabled() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(savedUser));
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        userService.enableUser(1L);

        assertEquals(1, savedUser.getEnabled());
        verify(userRepository).save(savedUser);
    }

    @Test
    void whenDisableUser_thenUserIsDisabled() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(savedUser));
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        userService.disableUser(1L);

        assertEquals(0, savedUser.getEnabled());
        verify(userRepository).save(savedUser);
    }

    @Test
    void whenChangePassword_thenPasswordIsUpdated() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(savedUser));
        when(passwordEncoder.matches("oldPassword", savedUser.getPassword())).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        userService.changePassword(1L, "oldPassword", "newPassword");

        assertEquals("encodedNewPassword", savedUser.getPassword());
        verify(userRepository).save(savedUser);
    }

    @Test
    void whenChangePasswordWithInvalidOldPassword_thenThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(savedUser));
        when(passwordEncoder.matches("wrongPassword", savedUser.getPassword())).thenReturn(false);

        assertThrows(BusinessException.class, 
            () -> userService.changePassword(1L, "wrongPassword", "newPassword"));
        verify(userRepository, never()).save(any(User.class));
    }
} 