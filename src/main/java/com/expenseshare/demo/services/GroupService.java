package com.expenseshare.demo.services;

import com.expenseshare.demo.dto.GroupDto;
import com.expenseshare.demo.entity.Group;
import com.expenseshare.demo.mapper.ExpenseMapper;
import com.expenseshare.demo.mapper.GroupMapper;
import com.expenseshare.demo.repository.GroupRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GroupService {
    private final GroupRepository groupRepository;

    public GroupService(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }
    public Group addGroup(GroupDto groupDto) {
      return  groupRepository.save(GroupMapper.INSTANCE.toEntity(groupDto));

    }

    public Group modifyGroup(GroupDto groupDto) {
        Optional<Group> group = groupRepository.findById(groupDto.getId());
        group.ifPresent(value -> value.setName(groupDto.getName()));
        return  groupRepository.save(group.get());
       }

    public void deleteGroup(Long id) {
        groupRepository.deleteById(id);
    }
}
