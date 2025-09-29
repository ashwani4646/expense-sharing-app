package com.expenseshare.demo;

import com.expenseshare.demo.dto.GroupDto;
import com.expenseshare.demo.entity.Group;
import com.expenseshare.demo.mapper.GroupMapper;
import com.expenseshare.demo.repository.GroupRepository;
import com.expenseshare.demo.services.GroupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GroupServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @InjectMocks
    private GroupService groupService;

    private GroupDto validGroupDto;
    private Group testGroup;
    private Group savedGroup;

    @BeforeEach
    void setUp() {
        validGroupDto = GroupDto.builder()
                .id(1L)
                .name("Test Group")
                .build();

        testGroup = Group.builder()
                .id(1L)
                .name("Test Group")
                .build();

        savedGroup = Group.builder()
                .id(1L)
                .name("Test Group")
                .build();
    }

    @Test
    void testAddGroup_Success() {
        // Arrange
        // Use the real mapper - it's just mapping fields
        Group mappedGroup = GroupMapper.INSTANCE.toEntity(validGroupDto);
        when(groupRepository.save(any(Group.class))).thenReturn(savedGroup);

        // Act
        Group result = groupService.addGroup(validGroupDto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Group", result.getName());

        verify(groupRepository).save(any(Group.class));
    }

    @Test
    void testAddGroup_NullGroupDto_ThrowsException() {
        // Act & Assert
        groupService.addGroup(null);

        // Optional: verify exception message if your service provides one
        //assertEquals("GroupDto cannot be null", exception.getMessage());

        verify(groupRepository, never()).save(any(Group.class));
    }



    @Test
    void testModifyGroup_Success() {
        // Arrange
        Group existingGroup = Group.builder()
                .id(1L)
                .name("Old Group Name")
                .build();

        Group modifiedGroup = Group.builder()
                .id(1L)
                .name("Test Group")
                .build();

        when(groupRepository.findById(1L)).thenReturn(Optional.of(existingGroup));
        when(groupRepository.save(existingGroup)).thenReturn(modifiedGroup);

        // Act
        Group result = groupService.modifyGroup(validGroupDto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Group", result.getName());
       
        verify(groupRepository).findById(1L);
        verify(groupRepository).save(existingGroup);
       
        // Verify that the name was actually set on the existing group
        assertEquals("Test Group", existingGroup.getName());
    }

    @Test
    void testModifyGroup_GroupNotFound_ThrowsException() {
        // Arrange
        when(groupRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () ->
            groupService.modifyGroup(validGroupDto));

        verify(groupRepository).findById(1L);
        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    void testModifyGroup_NullGroupDto_HandlesGracefully() {
        // Act & Assert
        assertThrows(NullPointerException.class, () ->
            groupService.modifyGroup(null));

        verify(groupRepository, never()).findById(anyLong());
        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    void testModifyGroup_NullGroupId_HandlesGracefully() {
        // Arrange
        GroupDto groupDtoWithNullId = GroupDto.builder()
                .id(null)
                .name("Test Group")
                .build();

        // Act & Assert
        assertThrows(NoSuchElementException.class, () ->
            groupService.modifyGroup(groupDtoWithNullId));


    }

    @Test
    void testModifyGroup_RepositoryFindFailure_PropagatesException() {
        // Arrange
        when(groupRepository.findById(1L))
                .thenThrow(new RuntimeException("Database query failed"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            groupService.modifyGroup(validGroupDto));

        assertEquals("Database query failed", exception.getMessage());
        verify(groupRepository).findById(1L);
        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    void testModifyGroup_RepositorySaveFailure_PropagatesException() {
        // Arrange
        Group existingGroup = Group.builder()
                .id(1L)
                .name("Old Group Name")
                .build();

        when(groupRepository.findById(1L)).thenReturn(Optional.of(existingGroup));
        when(groupRepository.save(existingGroup))
                .thenThrow(new RuntimeException("Database save failed"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            groupService.modifyGroup(validGroupDto));

        assertEquals("Database save failed", exception.getMessage());
        verify(groupRepository).findById(1L);
        verify(groupRepository).save(existingGroup);
    }

    @Test
    void testDeleteGroup_Success() {
        // Arrange
        doNothing().when(groupRepository).deleteById(1L);

        // Act
        groupService.deleteGroup(1L);

        // Assert
        verify(groupRepository).deleteById(1L);
    }

    @Test
    void testDeleteGroup_NullId_HandlesGracefully() {
        // Arrange
        doNothing().when(groupRepository).deleteById(null);

        // Act
        groupService.deleteGroup(null);

        // Assert
        verify(groupRepository).deleteById(null);
    }

    @Test
    void testDeleteGroup_RepositoryFailure_PropagatesException() {
        // Arrange
        doThrow(new RuntimeException("Database delete failed"))
                .when(groupRepository).deleteById(1L);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            groupService.deleteGroup(1L));

        assertEquals("Database delete failed", exception.getMessage());
        verify(groupRepository).deleteById(1L);
    }

    @Test
    void testDeleteGroup_NonExistentId_HandlesGracefully() {
        // Arrange
        Long nonExistentId = 999L;
        doNothing().when(groupRepository).deleteById(nonExistentId);

        // Act
        groupService.deleteGroup(nonExistentId);

        // Assert
        verify(groupRepository).deleteById(nonExistentId);
    }

    @Test
    void testModifyGroup_OnlyNameIsUpdated() {
        // Arrange
        Group existingGroup = Group.builder()
                .id(1L)
                .name("Original Name")
                .build();

        GroupDto updateDto = GroupDto.builder()
                .id(1L)
                .name("Updated Name")
                .build();

        when(groupRepository.findById(1L)).thenReturn(Optional.of(existingGroup));
        when(groupRepository.save(existingGroup)).thenReturn(existingGroup);

        // Act
        Group result = groupService.modifyGroup(updateDto);

        // Assert
        assertEquals("Updated Name", existingGroup.getName());
        assertEquals(1L, existingGroup.getId()); // ID should remain unchanged
        verify(groupRepository).findById(1L);
        verify(groupRepository).save(existingGroup);
    }

    @Test
    void testModifyGroup_EmptyName_AllowsEmptyName() {
        // Arrange
        Group existingGroup = Group.builder()
                .id(1L)
                .name("Original Name")
                .build();

        GroupDto updateDto = GroupDto.builder()
                .id(1L)
                .name("")
                .build();

        when(groupRepository.findById(1L)).thenReturn(Optional.of(existingGroup));
        when(groupRepository.save(existingGroup)).thenReturn(existingGroup);

        // Act
        Group result = groupService.modifyGroup(updateDto);

        // Assert
        assertEquals("", existingGroup.getName());
        verify(groupRepository).findById(1L);
        verify(groupRepository).save(existingGroup);
    }
}