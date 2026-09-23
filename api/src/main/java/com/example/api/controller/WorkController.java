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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.api.controller.request.work.WorkEditRequest;
import com.example.api.controller.request.work.WorkRegisterRequest;
import com.example.api.controller.response.WorkResponse;
import com.example.api.security.CustomUserDetails;
import com.example.api.service.AuthorizationService;
import com.example.api.service.WorkService;
import com.example.api.service.dto.work.WorkDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/works")
@RequiredArgsConstructor
public class WorkController {
    
    private final WorkService workService;
    private final AuthorizationService authorizationService;

    @GetMapping("/{tutorId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'TUTOR')")
    public ResponseEntity<List<WorkResponse>> findAll(
        @RequestParam Integer year,
        @RequestParam Integer month,
        @PathVariable UUID tutorId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        authorizationService.assertTutorAccess(userDetails, tutorId);

        return ResponseEntity.ok(
            workService.findAll(tutorId, year, month).stream()
                .map(WorkResponse::fromDto)
                .toList()
        );

    }

    @GetMapping("/{tutorId}/{workId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'TUTOR')")
    public ResponseEntity<WorkResponse> find(
        @PathVariable UUID tutorId,
        @PathVariable UUID workId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        authorizationService.assertTutorAccess(userDetails, tutorId);

        WorkDto work = workService.find(workId);
        authorizationService.assertResourceBelongsToTutor(tutorId, work.getTutorId());

        return ResponseEntity.ok(
            WorkResponse.fromDto(work)
        );

    }

    @PostMapping("/{tutorId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'TUTOR')")
    public ResponseEntity<WorkResponse> register(
        @Valid @RequestBody WorkRegisterRequest request,
        @PathVariable UUID tutorId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        authorizationService.assertPathId(tutorId, request.getTutorId());
        authorizationService.assertTutorAccess(userDetails, tutorId);

        return ResponseEntity.status(HttpStatus.CREATED).body(
            WorkResponse.fromDto(workService.register(request.toDto()))
        );

    }

    @PutMapping("/{tutorId}/{workId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'TUTOR')")
    public ResponseEntity<WorkResponse> edit(
        @Valid @RequestBody WorkEditRequest request,
        @PathVariable UUID tutorId,
        @PathVariable UUID workId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        authorizationService.assertPathId(tutorId, request.getTutorId());
        authorizationService.assertPathId(workId, request.getId());
        authorizationService.assertTutorAccess(userDetails, tutorId);

        WorkDto work = workService.find(workId);
        authorizationService.assertResourceBelongsToTutor(tutorId, work.getTutorId());

        return ResponseEntity.ok(
            WorkResponse.fromDto(workService.edit(request.toDto()))
        );

    }

    @DeleteMapping("/{tutorId}/{workId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'TUTOR')")
    public ResponseEntity<Void> delete(
        @PathVariable UUID tutorId,
        @PathVariable UUID workId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        authorizationService.assertTutorAccess(userDetails, tutorId);

        WorkDto work = workService.find(workId);
        authorizationService.assertResourceBelongsToTutor(tutorId, work.getTutorId());

        workService.delete(workId);

        return ResponseEntity.noContent().build();

    }

}
