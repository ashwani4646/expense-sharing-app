package com.expenseshare.demo.controller;

import com.expenseshare.demo.dto.*;
import com.expenseshare.demo.entity.Group;
import com.expenseshare.demo.entity.User;
import com.expenseshare.demo.services.UserGroupService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@Slf4j
public class UserGroupController {

    private final UserGroupService userGroupService;

    public  UserGroupController(UserGroupService userGroupService1){

        this.userGroupService = userGroupService1;
    }

    // User endpoints
    @PostMapping("/users")
    public ResponseEntity<User> createUser(@Valid @RequestBody UserDto request) {
        log.info("Received request to create user: {}", request.getUserName());
        User user = userGroupService.createUser(request);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("Received request to get all users");
        List<User> users = userGroupService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        log.info("Received request to get user with ID: {}", id);
        User user = userGroupService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/users/role")
    public ResponseEntity<UserResponseDto> updateUserRole(@AuthenticationPrincipal CustomOAuth2User principal,
                                                          @Valid @RequestBody UpdateUserRoleDto request) {
        log.info("Received request to update user {} role to {}", request.getUserId(), request.getRole());
        User user = principal.getUser();
        user.setRole(request.getRole());
        UserResponseDto userDto = userGroupService.updateUserRole(request, user);

        return ResponseEntity.ok(userDto);
    }
    // Group endpoints
    @PostMapping("/groups")
    public ResponseEntity<Group> createGroup(@Valid @RequestBody GroupDto groupDto) {
        log.info("Received request to create group: {}", groupDto.getName());
        Group group = userGroupService.createGroup(groupDto);
        return new ResponseEntity<>(group, HttpStatus.CREATED);
    }

    @GetMapping("/groups")
    public ResponseEntity<List<Group>> getAllGroups() {
        log.info("Received request to get all groups");
        List<Group> groups = userGroupService.getAllGroups();
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/groups/{id}")
    public ResponseEntity<Group> getGroupById(@PathVariable Long id) {
        log.info("Received request to get group with ID: {}", id);
        Group group = userGroupService.getGroupById(id);
        return ResponseEntity.ok(group);
    }

    // User-Group relationship endpoints
    @PostMapping("/groups/add-user")
    public ResponseEntity<ApiResponse> addUserToGroup(@Valid @RequestBody AddUserToGroupDto addUserToGroupDto) {
        log.info("Received request to add user {} to group {}", addUserToGroupDto.getUserId(), addUserToGroupDto.getGroupId());
        userGroupService.addUserToGroup(addUserToGroupDto);
        return ResponseEntity.ok(new ApiResponse("User successfully added to group"));
    }

    @DeleteMapping("/groups/{groupId}/users/{userId}")
    public ResponseEntity<ApiResponse> removeUserFromGroup(
            @PathVariable Long groupId,
            @PathVariable Long userId) {
        log.info("Received request to remove user {} from group {}", userId, groupId);
        userGroupService.removeUserFromGroup(userId, groupId);
        return ResponseEntity.ok(new ApiResponse("User successfully removed from group"));
    }

    // Response wrapper for simple messages
    @Data
    @AllArgsConstructor
    public static class ApiResponse {
        private String message;
    }
}

