package com.yofujitsu.booking_service.controller;

import com.yofujitsu.booking_service.dto.user.AuthRequest;
import com.yofujitsu.booking_service.dto.user.RegisterRequest;
import com.yofujitsu.booking_service.dto.user.TokenResponse;
import com.yofujitsu.booking_service.dto.user.UserCreateDto;
import com.yofujitsu.booking_service.dto.user.UserDto;
import com.yofujitsu.booking_service.dto.user.UserUpdateDto;
import com.yofujitsu.booking_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public TokenResponse register(@Valid @RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/auth")
    public TokenResponse authenticate(@Valid @RequestBody AuthRequest request) {
        return userService.authenticate(request);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserDto> getUsers() {
        return userService.getUsers();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@Valid @RequestBody UserCreateDto userCreateDto) {
        return userService.createUser(userCreateDto);
    }

    @PatchMapping
    @PreAuthorize("hasRole('ADMIN')")
    public UserDto updateUser(@Valid @RequestBody UserUpdateDto userUpdateDto) {
        return userService.updateUser(userUpdateDto);
    }

    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@RequestParam UUID id) {
        userService.deleteUser(id);
    }
}
