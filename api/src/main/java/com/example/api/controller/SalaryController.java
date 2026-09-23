package com.example.api.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.api.controller.request.salary.SalaryEditRequest;
import com.example.api.controller.request.salary.SalaryRegisterRequest;
import com.example.api.controller.response.SalaryResponse;
import com.example.api.security.CustomUserDetails;
import com.example.api.service.AuthorizationService;
import com.example.api.service.SalaryService;
import com.example.api.service.dto.SalaryDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/salaries")
@RequiredArgsConstructor
public class SalaryController {

    private final SalaryService salaryService;
    private final AuthorizationService authorizationService;

    @GetMapping("/{tutorId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'TUTOR')")
    public ResponseEntity<List<SalaryResponse>> findAll(
        @PathVariable UUID tutorId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        authorizationService.assertTutorAccess(userDetails, tutorId);

        return ResponseEntity.ok(
            salaryService.findAll(tutorId).stream()
                .map(SalaryResponse::fromDto)
                .toList()
        );
    }

    @GetMapping("/{tutorId}/{salaryId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'TUTOR')")
    public ResponseEntity<SalaryResponse> find(
        @PathVariable UUID tutorId,
        @PathVariable UUID salaryId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        authorizationService.assertTutorAccess(userDetails, tutorId);

        SalaryDto salary = salaryService.find(salaryId);
        authorizationService.assertResourceBelongsToTutor(tutorId, salary.getTutorId());

        return ResponseEntity.ok(
            SalaryResponse.fromDto(salary)
        );
    }

    @PostMapping("/{tutorId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<SalaryResponse> register(
        @Valid @RequestBody SalaryRegisterRequest request,
        @PathVariable UUID tutorId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        authorizationService.assertPathId(tutorId, request.getTutorId());
        authorizationService.assertTutorAccess(userDetails, tutorId);

        return ResponseEntity.status(HttpStatus.CREATED).body(
            SalaryResponse.fromDto(salaryService.register(request.toDto()))
        );
    }

    @PutMapping("/{tutorId}/{salaryId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<SalaryResponse> edit(
        @Valid @RequestBody SalaryEditRequest request,
        @PathVariable UUID tutorId,
        @PathVariable UUID salaryId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        authorizationService.assertPathId(tutorId, request.getTutorId());
        authorizationService.assertPathId(salaryId, request.getId());
        authorizationService.assertTutorAccess(userDetails, tutorId);

        SalaryDto salary = salaryService.find(salaryId);
        authorizationService.assertResourceBelongsToTutor(tutorId, salary.getTutorId());

        return ResponseEntity.ok(
            SalaryResponse.fromDto(salaryService.edit(request.toDto()))
        );
        
    }

    @DeleteMapping("/{tutorId}/{salaryId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> delete(
        @PathVariable UUID tutorId,
        @PathVariable UUID salaryId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        authorizationService.assertTutorAccess(userDetails, tutorId);

        SalaryDto salary = salaryService.find(salaryId);
        authorizationService.assertResourceBelongsToTutor(tutorId, salary.getTutorId());

        salaryService.delete(salaryId);
        return ResponseEntity.noContent().build();
    }
    
}
