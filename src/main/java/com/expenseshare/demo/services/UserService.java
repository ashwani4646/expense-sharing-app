package com.expenseshare.demo.services;

import com.expenseshare.demo.dto.UserDto;
import com.expenseshare.demo.entity.User;
import com.expenseshare.demo.mapper.UserMapper;
import com.expenseshare.demo.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private  final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User addUser(UserDto userDto) {
       return  userRepository.save(UserMapper.INSTANCE.toEntity(userDto));
    }
}
