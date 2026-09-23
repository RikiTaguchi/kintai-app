package com.example.api.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.api.controller.request.manager.ManagerEditRequest;
import com.example.api.controller.request.manager.ManagerPasswordChangeRequest;
import com.example.api.controller.request.manager.ManagerRegisterRequest;
import com.example.api.controller.response.ManagerResponse;
import com.example.api.security.CustomUserDetails;
import com.example.api.service.AuthorizationService;
import com.example.api.service.ManagerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/managers")
@RequiredArgsConstructor
public class ManagerController {
    
    private final ManagerService managerService;
    private final AuthorizationService authorizationService;

    @PostMapping
    public ResponseEntity<ManagerResponse> register(
        @Valid @RequestBody ManagerRegisterRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ManagerResponse.fromDto(managerService.register(request.toDto()))
        );
    }

    @PutMapping("/{managerId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ManagerResponse> edit(
        @Valid @RequestBody ManagerEditRequest request,
        @PathVariable UUID managerId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        authorizationService.assertPathId(managerId, request.getId());
        authorizationService.assertManagerSelf(userDetails, managerId);

        return ResponseEntity.ok(
            ManagerResponse.fromDto(managerService.edit(request.toDto()))
        );
    }

    @PutMapping("/{managerId}/my-password")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> changePassword(
        @Valid @RequestBody ManagerPasswordChangeRequest request,
        @PathVariable UUID managerId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        authorizationService.assertManagerSelf(userDetails, managerId);
        managerService.changePassword(managerId, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{managerId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> delete(
        @PathVariable UUID managerId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        authorizationService.assertManagerSelf(userDetails, managerId);
        
        managerService.delete(managerId);
        return ResponseEntity.noContent().build();
    }

}
