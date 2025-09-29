package com.expenseshare.demo;

import com.expenseshare.demo.dto.UserDto;
import com.expenseshare.demo.entity.User;
import com.expenseshare.demo.repository.UserRepository;
import com.expenseshare.demo.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private UserDto validUserDto;
    private User savedUser;

    @BeforeEach
    void setUp() {
        validUserDto = UserDto.builder()
                .userName("testuser")
                .emailId("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        savedUser = User.builder()
                .id(1L)
                .userName("testuser")
                .emailId("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();
    }

    @Test
    void testAddUser_Success() {
        // Arrange
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        User result = userService.addUser(validUserDto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUserName());
        assertEquals("test@example.com", result.getEmailId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());

        // Verify repository interaction
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertEquals("testuser", capturedUser.getUserName());
        assertEquals("test@example.com", capturedUser.getEmailId());
    }

    @Test
    void testAddUser_NullUserDto_ThrowsException() {
        // Act & Assert

        userService.addUser(null);
        // Verify repository was never called
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testAddUser_RepositorySaveFailure_PropagatesException() {
        // Arrange
        when(userRepository.save(any(User.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.addUser(validUserDto));

        assertEquals("Database connection failed", exception.getMessage());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testAddUser_WithMinimalUserDto_Success() {
        // Arrange
        UserDto minimalUserDto = UserDto.builder()
                .userName("minimaluser")
                .build();

        User savedMinimalUser = User.builder()
                .id(2L)
                .userName("minimaluser")
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedMinimalUser);

        // Act
        User result = userService.addUser(minimalUserDto);

        // Assert
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("minimaluser", result.getUserName());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void testAddUser_WithCompleteUserDto_Success() {
        // Arrange
        UserDto completeUserDto = UserDto.builder()
                .userName("completeuser")
                .emailId("complete@example.com")
                .firstName("Jane")
                .lastName("Smith")
                .build();

        User savedCompleteUser = User.builder()
                .id(3L)
                .userName("completeuser")
                .emailId("complete@example.com")
                .firstName("Jane")
                .lastName("Smith")
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedCompleteUser);

        // Act
        User result = userService.addUser(completeUserDto);

        // Assert
        assertNotNull(result);
        assertEquals(3L, result.getId());
        assertEquals("completeuser", result.getUserName());
        assertEquals("complete@example.com", result.getEmailId());
        assertEquals("Jane", result.getFirstName());
        assertEquals("Smith", result.getLastName());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void testAddUser_RepositoryReturnsNull_ReturnsNull() {
        // Arrange
        when(userRepository.save(any(User.class))).thenReturn(null);

        // Act
        User result = userService.addUser(validUserDto);

        // Assert
        assertNull(result, "Service should return null when repository returns null");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testAddUser_MultipleCallsWithSameUserDto_Success() {
        // Arrange
        User savedUser2 = User.builder()
                .id(2L)
                .userName("testuser")
                .emailId("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser)
                .thenReturn(savedUser2);

        // Act
        User result1 = userService.addUser(validUserDto);
        User result2 = userService.addUser(validUserDto);

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(1L, result1.getId());
        assertEquals(2L, result2.getId());
        assertEquals(result1.getUserName(), result2.getUserName());

        verify(userRepository, times(2)).save(any(User.class));
    }

    @Test
    void testAddUser_VerifyMappingBehavior() {
        // Arrange
        UserDto dtoWithSpecialChars = UserDto.builder()
                .userName("user@123")
                .emailId("special+email@example.com")
                .firstName("François")
                .lastName("O'Brien")
                .build();

        User savedSpecialUser = User.builder()
                .id(4L)
                .userName("user@123")
                .emailId("special+email@example.com")
                .firstName("François")
                .lastName("O'Brien")
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedSpecialUser);

        // Act
        User result = userService.addUser(dtoWithSpecialChars);

        // Assert
        assertNotNull(result);
        assertEquals("user@123", result.getUserName());
        assertEquals("special+email@example.com", result.getEmailId());
        assertEquals("François", result.getFirstName());
        assertEquals("O'Brien", result.getLastName());

        // Verify the mapped entity passed to repository
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertEquals(dtoWithSpecialChars.getUserName(), capturedUser.getUserName());
        assertEquals(dtoWithSpecialChars.getEmailId(), capturedUser.getEmailId());
    }

    @Test
    void testAddUser_WithNullFields_Success() {
        // Arrange
        UserDto dtoWithNulls = UserDto.builder()
                .userName("userwithnulls")
                .emailId(null)
                .firstName(null)
                .lastName(null)
                .build();

        User savedUserWithNulls = User.builder()
                .id(5L)
                .userName("userwithnulls")
                .emailId(null)
                .firstName(null)
                .lastName(null)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUserWithNulls);

        // Act
        User result = userService.addUser(dtoWithNulls);

        // Assert
        assertNotNull(result);
        assertEquals(5L, result.getId());
        assertEquals("userwithnulls", result.getUserName());
        assertNull(result.getEmailId());
        assertNull(result.getFirstName());
        assertNull(result.getLastName());

        verify(userRepository).save(any(User.class));
    }
}