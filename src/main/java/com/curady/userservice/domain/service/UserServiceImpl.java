package com.curady.userservice.domain.service;

import com.curady.userservice.advice.exception.NicknameAlreadyExistsException;
import com.curady.userservice.advice.exception.TendencyNotFoundException;
import com.curady.userservice.advice.exception.UserNotFoundException;
import com.curady.userservice.domain.entity.Tendency;
import com.curady.userservice.domain.entity.User;
import com.curady.userservice.domain.entity.UserTendency;
import com.curady.userservice.domain.mapper.UserMapper;
import com.curady.userservice.domain.repository.TendencyRepository;
import com.curady.userservice.domain.repository.UserRepository;
import com.curady.userservice.domain.repository.UserTendencyRepository;
import com.curady.userservice.web.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final TendencyRepository tendencyRepository;
    private final UserTendencyRepository userTendencyRepository;

    @Override
    @Transactional
    public ResponseSignup createUserInfo(RequestUserInfo request, String id) {
        if (userRepository.findByNickname(request.getNickname()).isPresent())
            throw new NicknameAlreadyExistsException();

        User user = userRepository.findById(Long.valueOf(id)).orElseThrow(UserNotFoundException::new);
        user.updateUserInfo(request);

        List<RequestTendency> requestTendency = request.getRequestTendencies();
        requestTendency.forEach(v -> {
            Tendency tendency = tendencyRepository.findByNameAndType(v.getTendencyName(), v.getTendencyType()).orElseThrow(TendencyNotFoundException::new);

            Optional<UserTendency> userTendency = userTendencyRepository.findByUserAndTendency(user, tendency);
            if (userTendency.isEmpty()) {
                userTendencyRepository.save(new UserTendency(user, tendency));
            }
        });

        return ResponseSignup.builder()
                .id(user.getId())
                .email(user.getEmail())
                .build();
    }

    @Override
    public ResponseUserInfo getUserInfo(String id) {
        User user = userRepository.findById(Long.valueOf(id)).orElseThrow(UserNotFoundException::new);
        return UserMapper.INSTANCE.entityToResponse(user);
    }

    @Override
    public Boolean checkUserEmailAuth(Long id) {
        User user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);
        return user.isEmailAuth();
    }

    @Override
    public List<ResponseUserNicknameAndImage> getUsersNicknameAndImage(List<Long> list) {
        List<User> users = new ArrayList<>();
        list.forEach(v -> {
            users.add(userRepository.findById(v).orElseThrow(UserNotFoundException::new));
        });
        return UserMapper.INSTANCE.usersToResponseList(users);
    }
}
