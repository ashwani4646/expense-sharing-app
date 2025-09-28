package com.expenseshare.demo.services;

import com.expenseshare.demo.dto.*;
import com.expenseshare.demo.entity.Group;
import com.expenseshare.demo.entity.User;
import com.expenseshare.demo.enums.Role;
import com.expenseshare.demo.exception.DuplicateResourceException;
import com.expenseshare.demo.exception.ResourceNotFoundException;
import com.expenseshare.demo.mapper.GroupMapper;
import com.expenseshare.demo.repository.GroupRepository;
import com.expenseshare.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@Slf4j
public class UserGroupService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;

    public UserGroupService(UserRepository userRepository, GroupRepository groupRepository){

        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
    }
    public User createUser(UserDto request) {
        log.info("Creating user with username: {}", request.getUserName());

        // Check if username already exists
        if (userRepository.existsByUserName(request.getUserName())) {
            throw new DuplicateResourceException("Username already exists: " + request.getUserName());
        }

        // Check if email already exists
        if (userRepository.existsByEmailId(request.getEmailId())) {
            throw new DuplicateResourceException("Email already exists: " + request.getEmailId());
        }

        User user = User.builder()
                .userName(request.getUserName())
                .emailId(request.getEmailId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .build();

        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());
        return savedUser;
    }

    public Group createGroup(GroupDto groupDto) {
        log.info("Creating group with name: {}", groupDto.getName());

        // Check if group name already exists
        if (groupRepository.existsByName(groupDto.getName())) {
            throw new DuplicateResourceException("Group name already exists: " + groupDto.getName());
        }

        Group savedGroup = groupRepository.save(GroupMapper.INSTANCE.toEntity(groupDto));
        log.info("Group created successfully with ID: {}", savedGroup.getId());
        return savedGroup;
    }

    public void addUserToGroup(AddUserToGroupDto request) {
        log.info("Adding user ID {} to group ID {}", request.getUserId(), request.getGroupId());

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getUserId()));

        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with ID: " + request.getGroupId()));

        if (group.getUsers().contains(user)) {
            log.warn("User {} is already in group {}", user.getUserName(), group.getName());
            return;
        }

        group.addUser(user);
        groupRepository.save(group);
        log.info("User {} successfully added to group {}", user.getUserName(), group.getName());
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Group> getAllGroups() {
        log.info("Fetching all groups");
        return groupRepository.findAll();
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        log.info("Fetching user with ID: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public Group getGroupById(Long id) {
        log.info("Fetching group with ID: {}", id);
        return groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with ID: " + id));
    }

    public void removeUserFromGroup(Long userId, Long groupId) {
        log.info("Removing user ID {} from group ID {}", userId, groupId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with ID: " + groupId));

        group.removeUser(user);
        groupRepository.save(group);
        log.info("User {} successfully removed from group {}", user.getUserName(), group.getName());
    }

    public Group modifyGroup(GroupDto groupDto) {
        Optional<Group> group = groupRepository.findById(groupDto.getId());
        group.ifPresent(value -> value.setName(groupDto.getName()));
        return  groupRepository.save(group.get());
    }

    public void deleteGroup(Long id) {
        groupRepository.deleteById(id);
    }

    public UserResponseDto updateUserRole(UpdateUserRoleDto updateUserRoleDto) {
        log.info("Updating role for user ID {} to {}", updateUserRoleDto.getUserId(), updateUserRoleDto.getRole());

        User user = userRepository.findById(updateUserRoleDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + updateUserRoleDto.getUserId()));

        Role oldRole = user.getRole();
        user.setRole(updateUserRoleDto.getRole());
        User updatedUser = userRepository.save(user);

        log.info("User {} role updated from {} to {}",
                user.getUserName(), oldRole, updateUserRoleDto.getRole());
        return convertToUserResponse(updatedUser);
    }

    private UserResponseDto convertToUserResponse(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .username(user.getUserName())
                .email(user.getEmailId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}