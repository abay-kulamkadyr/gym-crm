package com.epam.interfaces.web.controller.impl;

import com.epam.infrastructure.security.core.AuthenticationResult;
import com.epam.infrastructure.security.port.in.AuthenticationUseCase;
import com.epam.infrastructure.security.port.in.PasswordManagementUseCase;
import com.epam.interfaces.web.controller.api.UserControllerApi;
import com.epam.interfaces.web.dto.request.ChangePasswordRequest;
import com.epam.interfaces.web.dto.request.LoginRequest;
import com.epam.interfaces.web.dto.response.JwtResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class UserController implements UserControllerApi {

    private final AuthenticationUseCase authenticationUseCase;

    private final PasswordManagementUseCase passwordManagementUseCase;

    @Autowired
    UserController(AuthenticationUseCase authenticationUseCase, PasswordManagementUseCase passwordManagementUseCase) {
        this.authenticationUseCase = authenticationUseCase;
        this.passwordManagementUseCase = passwordManagementUseCase;
    }

    @Override
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for username: {}", request.username());
        AuthenticationResult result = authenticationUseCase.authenticate(request.username(), request.password());
        return ResponseEntity.ok(new JwtResponse(result.token()));
    }

    @Override
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization") String authHeader) {
        if (authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            authenticationUseCase.logout(token);
        }
        return ResponseEntity.noContent().build();
    }

    @Override
    @PutMapping("/password")
    @PreAuthorize("#username == authentication.name")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {
        String username = userDetails.getUsername();
        log.info("Password change request for user: {}", username);
        passwordManagementUseCase.changePassword(username, request.oldPassword(), request.newPassword());
        return ResponseEntity.ok().build();
    }

}
