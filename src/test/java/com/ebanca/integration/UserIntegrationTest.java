package com.ebanca.integration;

import com.ebanca.exception.BusinessException;
import com.ebanca.exception.ResourceNotFoundException;
import com.ebanca.model.User;
import com.ebanca.repository.UserRepository;
import com.ebanca.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Nettoyer la base de données avant chaque test
        userRepository.deleteAll();

        // Créer un utilisateur de test
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("Password123!");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setEnabled(1);
    }

    @Test
    void whenCreateUser_thenUserIsSaved() {
        User savedUser = userService.createUser(testUser);

        assertNotNull(savedUser.getId());
        assertEquals(testUser.getUsername(), savedUser.getUsername());
        assertEquals(testUser.getEmail(), savedUser.getEmail());
        assertEquals(1, savedUser.getEnabled());
    }

    @Test
    void whenCreateUserWithExistingUsername_thenThrowException() {
        userService.createUser(testUser);
        
        User anotherUser = new User();
        anotherUser.setUsername(testUser.getUsername());
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword("Password123!");
        
        assertThrows(BusinessException.class, () -> userService.createUser(anotherUser));
    }

    @Test
    void whenFindByUsername_thenUserIsReturned() {
        User savedUser = userService.createUser(testUser);
        User foundUser = userService.findByUsername(testUser.getUsername()).orElseThrow();

        assertNotNull(foundUser);
        assertEquals(savedUser.getId(), foundUser.getId());
        assertEquals(savedUser.getUsername(), foundUser.getUsername());
    }

    @Test
    void whenFindByUsernameWithInvalidUsername_thenThrowException() {
        assertThrows(ResourceNotFoundException.class, 
            () -> userService.findByUsername("invalid").orElseThrow(() -> new ResourceNotFoundException("User not found")));
    }

    @Test
    void whenUpdateUser_thenUserIsUpdated() {
        User savedUser = userService.createUser(testUser);
        
        User updatedUser = new User();
        updatedUser.setFirstName("Updated");
        updatedUser.setLastName("Name");
        
        User result = userService.updateUser(savedUser.getId(), updatedUser);

        assertEquals("Updated", result.getFirstName());
        assertEquals("Name", result.getLastName());
        assertEquals(savedUser.getUsername(), result.getUsername());
    }

    @Test
    void whenDeleteUser_thenUserIsDeleted() {
        User savedUser = userService.createUser(testUser);
        userService.deleteById(savedUser.getId());

        assertThrows(ResourceNotFoundException.class, 
            () -> userService.findById(savedUser.getId()).orElseThrow(() -> new ResourceNotFoundException("User not found")));
    }

    @Test
    void whenFindAll_thenAllUsersAreReturned() {
        userService.createUser(testUser);
        
        User anotherUser = new User();
        anotherUser.setUsername("anotheruser");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword("Password123!");
        anotherUser.setFirstName("Another");
        anotherUser.setLastName("User");
        userService.createUser(anotherUser);

        List<User> users = userService.findAll();

        assertEquals(2, users.size());
    }

    @Test
    void whenEnableUser_thenUserIsEnabled() {
        User savedUser = userService.createUser(testUser);
        savedUser.setEnabled(0);
        userRepository.save(savedUser);

        userService.enableUser(savedUser.getId());

        User updatedUser = userService.findById(savedUser.getId()).orElseThrow();
        assertEquals(1, updatedUser.getEnabled());
    }

    @Test
    void whenDisableUser_thenUserIsDisabled() {
        User savedUser = userService.createUser(testUser);

        userService.disableUser(savedUser.getId());

        User updatedUser = userService.findById(savedUser.getId()).orElseThrow();
        assertEquals(0, updatedUser.getEnabled());
    }

    @Test
    void whenChangePassword_thenPasswordIsUpdated() {
        User savedUser = userService.createUser(testUser);
        String newPassword = "NewPassword123!";

        userService.changePassword(savedUser.getId(), "Password123!", newPassword);

        User updatedUser = userService.findById(savedUser.getId()).orElseThrow();
        assertNotEquals(testUser.getPassword(), updatedUser.getPassword());
    }

    @Test
    void whenChangePasswordWithInvalidOldPassword_thenThrowException() {
        User savedUser = userService.createUser(testUser);

        assertThrows(BusinessException.class, 
            () -> userService.changePassword(savedUser.getId(), "WrongPassword", "NewPassword123!"));
    }
} 