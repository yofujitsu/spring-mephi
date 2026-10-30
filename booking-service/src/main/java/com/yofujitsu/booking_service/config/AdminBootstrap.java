package com.yofujitsu.booking_service.config;

import com.yofujitsu.booking_service.entity.Role;
import com.yofujitsu.booking_service.entity.User;
import com.yofujitsu.booking_service.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class AdminBootstrap implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String password;

    public AdminBootstrap(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap.admin.username:admin}") String username,
            @Value("${app.bootstrap.admin.password:admin123}") String password) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.username = username;
        this.password = password;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.existsByUsername(username)) {
            return;
        }

        userRepository.save(User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .role(Role.ADMIN)
                .build());
        log.warn("Bootstrap admin '{}' created, change the password before real usage", username);
    }
}
