package com.expenseshare.demo.services;

import com.expenseshare.demo.dto.GroupDto;
import com.expenseshare.demo.mapper.ExpenseMapper;
import com.expenseshare.demo.mapper.GroupMapper;
import com.expenseshare.demo.repository.GroupRepository;
import org.springframework.stereotype.Service;

@Service
public class GroupService {
    private final GroupRepository groupRepository;

    public GroupService(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }
    public void addGroup(GroupDto group) {
        groupRepository.save(GroupMapper.INSTANCE.toEntity(group));

    }
}
