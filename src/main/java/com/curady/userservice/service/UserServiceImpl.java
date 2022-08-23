package com.curady.userservice.service;

import com.curady.userservice.dto.UserDto;
import com.curady.userservice.mapper.UserMapper;
import com.curady.userservice.model.User;
import com.curady.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    UserRepository userRepository;
    BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void createUser(UserDto userDto) {
        userDto.setUuid(UUID.randomUUID().toString());

        User user = UserMapper.INSTANCE.dtoToEntity(userDto);
        user.setEncryptedPwd(passwordEncoder.encode(userDto.getPassword()));
        user.setRoles("ROLE_USER");

        userRepository.save(user);
    }
}
