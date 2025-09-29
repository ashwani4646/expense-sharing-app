package com.expenseshare.demo;

import com.expenseshare.demo.dto.*;
import com.expenseshare.demo.entity.Group;
import com.expenseshare.demo.entity.User;
import com.expenseshare.demo.enums.Role;
import com.expenseshare.demo.exception.DuplicateResourceException;
import com.expenseshare.demo.exception.ResourceNotFoundException;
import com.expenseshare.demo.repository.GroupRepository;
import com.expenseshare.demo.repository.UserRepository;
import com.expenseshare.demo.services.UserGroupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserGroupServiceTest {

    @Mock
    private UserRepository userRepository;
   
    @Mock
    private GroupRepository groupRepository;
   
    @InjectMocks
    private UserGroupService userGroupService;
   
    private UserDto validUserDto;
    private GroupDto validGroupDto;
    private User testUser;
    private Group testGroup;
    private AddUserToGroupDto addUserToGroupDto;
    private UpdateUserRoleDto updateUserRoleDto;

    @BeforeEach
    void setUp() {
        validUserDto = UserDto.builder()
                .userName("testuser")
                .emailId("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();

        validGroupDto = GroupDto.builder()
                .id(1L)
                .name("Test Group")
                .build();

        testUser = User.builder()
                .id(1L)
                .userName("testuser")
                .emailId("test@example.com")
                .firstName("Test")
                .lastName("User")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        testGroup = Group.builder()
                .id(1L)
                .name("Test Group")
                .users(new HashSet<>())
                .build();

        addUserToGroupDto = AddUserToGroupDto.builder()
                .userId(1L)
                .groupId(1L)
                .build();

        updateUserRoleDto = UpdateUserRoleDto.builder()
                .userId(1L)
                .role(Role.ADMIN)
                .build();
    }

    @Test
    void testCreateUser_Success() {
        // Arrange
        when(userRepository.existsByUserName("testuser")).thenReturn(false);
        when(userRepository.existsByEmailId("test@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userGroupService.createUser(validUserDto);

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUserName());
        assertEquals("test@example.com", result.getEmailId());
        assertEquals("Test", result.getFirstName());
        assertEquals("User", result.getLastName());
       
        verify(userRepository).existsByUserName("testuser");
        verify(userRepository).existsByEmailId("test@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testCreateUser_DuplicateUsername_ThrowsException() {
        // Arrange
        when(userRepository.existsByUserName("testuser")).thenReturn(true);

        // Act & Assert
        DuplicateResourceException exception = assertThrows(
            DuplicateResourceException.class,
            () -> userGroupService.createUser(validUserDto)
        );
       
        assertEquals("Username already exists: testuser", exception.getMessage());
        verify(userRepository).existsByUserName("testuser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testCreateUser_DuplicateEmail_ThrowsException() {
        // Arrange
        when(userRepository.existsByUserName("testuser")).thenReturn(false);
        when(userRepository.existsByEmailId("test@example.com")).thenReturn(true);

        // Act & Assert
        DuplicateResourceException exception = assertThrows(
            DuplicateResourceException.class,
            () -> userGroupService.createUser(validUserDto)
        );
       
        assertEquals("Email already exists: test@example.com", exception.getMessage());
        verify(userRepository).existsByUserName("testuser");
        verify(userRepository).existsByEmailId("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testCreateGroup_Success() {
        // Arrange
        when(groupRepository.existsByName("Test Group")).thenReturn(false);
        when(groupRepository.save(any(Group.class))).thenReturn(testGroup);

        // Act
        Group result = userGroupService.createGroup(validGroupDto);

        // Assert
        assertNotNull(result);
        assertEquals("Test Group", result.getName());
        assertEquals(1L, result.getId());
       
        verify(groupRepository).existsByName("Test Group");
        verify(groupRepository).save(any(Group.class));
    }

    @Test
    void testCreateGroup_DuplicateName_ThrowsException() {
        // Arrange
        when(groupRepository.existsByName("Test Group")).thenReturn(true);

        // Act & Assert
        DuplicateResourceException exception = assertThrows(
            DuplicateResourceException.class,
            () -> userGroupService.createGroup(validGroupDto)
        );
       
        assertEquals("Group name already exists: Test Group", exception.getMessage());
        verify(groupRepository).existsByName("Test Group");
        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    void testAddUserToGroup_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(groupRepository.findById(1L)).thenReturn(Optional.of(testGroup));
        when(groupRepository.save(any(Group.class))).thenReturn(testGroup);

        // Act
        userGroupService.addUserToGroup(addUserToGroupDto);

        // Assert
        verify(userRepository).findById(1L);
        verify(groupRepository).findById(1L);
        verify(groupRepository).save(testGroup);
    }

    @Test
    void testAddUserToGroup_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> userGroupService.addUserToGroup(addUserToGroupDto)
        );
       
        assertEquals("User not found with ID: 1", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(groupRepository, never()).findById(anyLong());
    }

    @Test
    void testAddUserToGroup_GroupNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(groupRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> userGroupService.addUserToGroup(addUserToGroupDto)
        );
       
        assertEquals("Group not found with ID: 1", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(groupRepository).findById(1L);
        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    void testAddUserToGroup_UserAlreadyInGroup() {
        // Arrange
        testGroup.getUsers().add(testUser); // User already in group
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(groupRepository.findById(1L)).thenReturn(Optional.of(testGroup));

        // Act
        userGroupService.addUserToGroup(addUserToGroupDto);

        // Assert
        verify(userRepository).findById(1L);
        verify(groupRepository).findById(1L);
        // Should not save since user is already in group
        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    void testGetAllUsers_Success() {
        // Arrange
        List<User> expectedUsers = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(expectedUsers);

        // Act
        List<User> result = userGroupService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUser, result.get(0));
        verify(userRepository).findAll();
    }

    @Test
    void testGetAllGroups_Success() {
        // Arrange
        List<Group> expectedGroups = Arrays.asList(testGroup);
        when(groupRepository.findAll()).thenReturn(expectedGroups);

        // Act
        List<Group> result = userGroupService.getAllGroups();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testGroup, result.get(0));
        verify(groupRepository).findAll();
    }

    @Test
    void testGetUserById_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        User result = userGroupService.getUserById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testUser, result);
        verify(userRepository).findById(1L);
    }

    @Test
    void testGetUserById_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> userGroupService.getUserById(1L)
        );
       
        assertEquals("User not found with ID: 1", exception.getMessage());
        verify(userRepository).findById(1L);
    }

    @Test
    void testGetGroupById_Success() {
        // Arrange
        when(groupRepository.findById(1L)).thenReturn(Optional.of(testGroup));

        // Act
        Group result = userGroupService.getGroupById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testGroup, result);
        verify(groupRepository).findById(1L);
    }

    @Test
    void testGetGroupById_GroupNotFound_ThrowsException() {
        // Arrange
        when(groupRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> userGroupService.getGroupById(1L)
        );
       
        assertEquals("Group not found with ID: 1", exception.getMessage());
        verify(groupRepository).findById(1L);
    }

    @Test
    void testRemoveUserFromGroup_Success() {
        // Arrange
        testGroup.getUsers().add(testUser); // Add user to group first
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(groupRepository.findById(1L)).thenReturn(Optional.of(testGroup));
        when(groupRepository.save(any(Group.class))).thenReturn(testGroup);

        // Act
        userGroupService.removeUserFromGroup(1L, 1L);

        // Assert
        verify(userRepository).findById(1L);
        verify(groupRepository).findById(1L);
        verify(groupRepository).save(testGroup);
    }

    @Test
    void testRemoveUserFromGroup_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> userGroupService.removeUserFromGroup(1L, 1L)
        );
       
        assertEquals("User not found with ID: 1", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(groupRepository, never()).findById(anyLong());
    }

    @Test
    void testRemoveUserFromGroup_GroupNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(groupRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> userGroupService.removeUserFromGroup(1L, 1L)
        );
       
        assertEquals("Group not found with ID: 1", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(groupRepository).findById(1L);
        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    void testModifyGroup_Success() {
        // Arrange
        Group existingGroup = Group.builder()
                .id(1L)
                .name("Old Name")
                .users(new HashSet<>())
                .build();
       
        Group updatedGroup = Group.builder()
                .id(1L)
                .name("Test Group")
                .users(new HashSet<>())
                .build();

        when(groupRepository.findById(1L)).thenReturn(Optional.of(existingGroup));
        when(groupRepository.save(any(Group.class))).thenReturn(updatedGroup);

        // Act
        Group result = userGroupService.modifyGroup(validGroupDto);

        // Assert
        assertNotNull(result);
        assertEquals("Test Group", result.getName());
        verify(groupRepository).findById(1L);
        verify(groupRepository).save(any(Group.class));
    }

    @Test
    void testModifyGroup_GroupNotFound() {
        // Arrange
        when(groupRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () ->
            userGroupService.modifyGroup(validGroupDto));
       
        verify(groupRepository).findById(1L);
        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    void testDeleteGroup_Success() {
        // Act
        userGroupService.deleteGroup(1L);

        // Assert
        verify(groupRepository).deleteById(1L);
    }

    @Test
    void testUpdateUserRole_Success() {
        // Arrange
        User updatingUser = User.builder()
                .role(Role.ADMIN)
                .build();
       
        User updatedUser = User.builder()
                .id(1L)
                .userName("testuser")
                .emailId("test@example.com")
                .firstName("Test")
                .lastName("User")
                .role(Role.ADMIN)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // Act
        UserResponseDto result = userGroupService.updateUserRole(updateUserRoleDto, updatingUser);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals(Role.ADMIN, result.getRole());
       
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testUpdateUserRole_UserNotFound_ThrowsException() {
        // Arrange
        User updatingUser = User.builder().role(Role.ADMIN).build();
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> userGroupService.updateUserRole(updateUserRoleDto, updatingUser)
        );
       
        assertEquals("User not found with ID: 1", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }

    // Edge case tests
    @Test
    void testCreateUser_NullUserDto_HandlesGracefully() {
        // Act & Assert
        assertThrows(NullPointerException.class, () ->
            userGroupService.createUser(null));
    }

    @Test
    void testCreateGroup_NullGroupDto_HandlesGracefully() {
        // Act & Assert
        assertThrows(NullPointerException.class, () ->
            userGroupService.createGroup(null));
    }

    @Test
    void testAddUserToGroup_NullRequest_HandlesGracefully() {
        // Act & Assert
        assertThrows(NullPointerException.class, () ->
            userGroupService.addUserToGroup(null));
    }

    @Test
    void testGetAllUsers_EmptyList() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<User> result = userGroupService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository).findAll();
    }

    @Test
    void testGetAllGroups_EmptyList() {
        // Arrange
        when(groupRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<Group> result = userGroupService.getAllGroups();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(groupRepository).findAll();
    }
}