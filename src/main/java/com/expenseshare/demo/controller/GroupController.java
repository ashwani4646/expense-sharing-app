package com.expenseshare.demo.controller;

import com.expenseshare.demo.dto.GroupDto;
import com.expenseshare.demo.entity.Group;
import com.expenseshare.demo.services.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("(/groups")
public class GroupController {
    private final  GroupService groupService;

    public GroupController(@Autowired GroupService groupService){
        this.groupService = groupService;
    }

    @PostMapping
    ResponseEntity<GroupDto> addGroup(@RequestBody GroupDto group){
        groupService.addGroup(group);
        return  new ResponseEntity<>(group, HttpStatus.OK);
    }


    @PutMapping
    ResponseEntity<GroupDto> modifyGroup(@RequestBody GroupDto group){
        return  new ResponseEntity<>(group, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteGroup(@PathVariable Long id){
        return  new ResponseEntity<>(HttpStatus.OK);
    }

}
