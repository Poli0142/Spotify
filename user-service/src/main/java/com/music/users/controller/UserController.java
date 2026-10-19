package com.music.users.controller;

import com.music.users.dto.LoginRequest;
import com.music.users.dto.UserRequest;
import com.music.users.exception.ConflictException;
import com.music.users.exception.NotFoundException;
import com.music.users.exception.UnauthorizedException;
import com.music.users.model.Role;
import com.music.users.model.User;
import com.music.users.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Пользователи")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    @Operation(summary = "Вход по логину и паролю")
    @ApiResponse(responseCode = "401", description = "Неверный логин или пароль")
    public User login(@Valid @RequestBody LoginRequest request) {
        User user = repository.findByUsername(request.username())
                .filter(u -> passwordEncoder.matches(request.password(), u.getPassword()))
                .orElseThrow(() -> new UnauthorizedException("Неверный логин или пароль"));
        log.info("Вход: {} ({})", user.getUsername(), user.getRole());
        return user;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Регистрация пользователя")
    @ApiResponse(responseCode = "409", description = "Логин занят")
    public User register(@Valid @RequestBody UserRequest request) {
        if (request.role() == Role.ADMIN) {
            throw new IllegalArgumentException("Администратора нельзя зарегистрировать");
        }
        if (repository.existsByUsername(request.username())) {
            throw new ConflictException("Логин " + request.username() + " уже занят");
        }
        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setDisplayName(request.displayName());
        user.setRole(request.role() == null ? Role.USER : request.role());
        user = repository.save(user);
        log.info("Зарегистрирован пользователь {} ({}, {})", user.getId(), user.getUsername(), user.getRole());
        return user;
    }

    @GetMapping
    @Operation(summary = "Все пользователи")
    public List<User> getAll() {
        log.info("Список пользователей");
        return repository.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Пользователь по id")
    @ApiResponse(responseCode = "404", description = "Не найден")
    public User getById(@PathVariable Long id) {
        log.info("Пользователь {}", id);
        return find(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить пользователя")
    public void delete(@PathVariable Long id) {
        repository.delete(find(id));
        log.info("Удалён пользователь {}", id);
    }

    private User find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Пользователь " + id + " не найден"));
    }
}
