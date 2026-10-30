package com.yofujitsu.booking_service.service;

import com.yofujitsu.booking_service.dto.user.AuthRequest;
import com.yofujitsu.booking_service.dto.user.RegisterRequest;
import com.yofujitsu.booking_service.dto.user.TokenResponse;
import com.yofujitsu.booking_service.dto.user.UserCreateDto;
import com.yofujitsu.booking_service.dto.user.UserDto;
import com.yofujitsu.booking_service.dto.user.UserUpdateDto;
import com.yofujitsu.booking_service.entity.Role;
import com.yofujitsu.booking_service.entity.User;
import com.yofujitsu.booking_service.exception.ConflictException;
import com.yofujitsu.booking_service.exception.InvalidCredentialsException;
import com.yofujitsu.booking_service.exception.ResourceNotFoundException;
import com.yofujitsu.booking_service.mapper.UserMapper;
import com.yofujitsu.booking_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public TokenResponse register(RegisterRequest request) {
        ensureUsernameFree(request.username());

        User user = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .build();
        userRepository.save(user);

        log.info("Registered user {}", user.getUsername());
        return jwtService.issueToken(user);
    }

    @Transactional(readOnly = true)
    public TokenResponse authenticate(AuthRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new InvalidCredentialsException(
                        "Invalid username or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        return jwtService.issueToken(user);
    }

    @Transactional(readOnly = true)
    public List<UserDto> getUsers() {
        return userMapper.toDtos(userRepository.findAll(Sort.by("username")));
    }

    @Transactional
    public UserDto createUser(UserCreateDto userCreateDto) {
        ensureUsernameFree(userCreateDto.username());

        User user = User.builder()
                .username(userCreateDto.username())
                .password(passwordEncoder.encode(userCreateDto.password()))
                .role(userCreateDto.role())
                .build();
        userRepository.save(user);

        log.info("Created user {} with role {}", user.getUsername(), user.getRole());
        return userMapper.toDto(user);
    }

    @Transactional
    public UserDto updateUser(UserUpdateDto userUpdateDto) {
        User user = getUserOrThrow(userUpdateDto.id());

        if (StringUtils.hasText(userUpdateDto.username())
                && !userUpdateDto.username().equals(user.getUsername())) {
            if (userRepository.existsByUsernameAndIdNot(
                    userUpdateDto.username(), user.getId())) {
                throw new ConflictException("Username already exists: "
                        + userUpdateDto.username());
            }
            user.setUsername(userUpdateDto.username());
        }
        if (StringUtils.hasText(userUpdateDto.password())) {
            user.setPassword(passwordEncoder.encode(userUpdateDto.password()));
        }
        if (userUpdateDto.role() != null) {
            user.setRole(userUpdateDto.role());
        }

        log.info("Updated user {}", user.getId());
        return userMapper.toDto(user);
    }

    @Transactional
    public void deleteUser(UUID id) {
        User user = getUserOrThrow(id);
        userRepository.delete(user);
        log.info("Deleted user {}", id);
    }

    private void ensureUsernameFree(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new ConflictException("Username already exists: " + username);
        }
    }

    private User getUserOrThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }
}
