package com.example.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.api.controller.request.manager.ManagerLoginRequest;
import com.example.api.controller.request.tutor.TutorLoginRequest;
import com.example.api.controller.response.ManagerResponse;
import com.example.api.controller.response.TutorResponse;
import com.example.api.service.LoginService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LoginController {
    
    private final LoginService loginService;

    @PostMapping("/managers/login")
    public ResponseEntity<ManagerResponse> loginManager(
        @Valid @RequestBody ManagerLoginRequest request,
        HttpServletRequest httpRequest,
        HttpServletResponse httpResponse
    ) {
        return ResponseEntity.ok(ManagerResponse.fromDto(
            loginService.loginManager(request.toDto(), httpRequest, httpResponse)
        ));
    }

    @PostMapping("/tutors/login")
    public ResponseEntity<TutorResponse> loginTutor(
        @Valid @RequestBody TutorLoginRequest request,
        HttpServletRequest httpRequest,
        HttpServletResponse httpResponse
    ) {
        return ResponseEntity.ok(TutorResponse.fromDto(
            loginService.loginTutor(request.toDto(), httpRequest, httpResponse)
        ));
    }

}
