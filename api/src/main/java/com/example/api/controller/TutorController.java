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

import com.example.api.controller.request.tutor.TutorEditRequest;
import com.example.api.controller.request.tutor.TutorPasswordChangeRequest;
import com.example.api.controller.request.tutor.TutorPasswordResetRequest;
import com.example.api.controller.request.tutor.TutorRegisterRequest;
import com.example.api.controller.response.TutorResponse;
import com.example.api.security.CustomUserDetails;
import com.example.api.service.AuthorizationService;
import com.example.api.service.ManagerService;
import com.example.api.service.TutorService;
import com.example.api.service.dto.TutorDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tutors")
@RequiredArgsConstructor
public class TutorController {
    
    private final TutorService tutorService;
    private final ManagerService managerService;
    private final AuthorizationService authorizationService;

    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<TutorResponse>> findAll(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
            tutorService.findAll(
                managerService.find(userDetails.getId()).getClassroomId()
            ).stream()
                .map(TutorResponse::fromDto)
                .toList()
        );

    }

    @GetMapping("/{tutorId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<TutorResponse> find(
        @PathVariable UUID tutorId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TutorDto tutor = tutorService.find(tutorId);
        authorizationService.assertManagerOwnsTutor(userDetails, tutor);
        return ResponseEntity.ok(TutorResponse.fromDto(tutor));
    }

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<TutorResponse> register(
        @Valid @RequestBody TutorRegisterRequest request,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        authorizationService.assertManagerClassroom(userDetails, request.getClassroomId());

        return ResponseEntity.status(HttpStatus.CREATED).body(
            TutorResponse.fromDto(tutorService.register(request.toDto()))
        );
    }

    @PutMapping("/{tutorId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<TutorResponse> edit(
        @Valid @RequestBody TutorEditRequest request,
        @PathVariable UUID tutorId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        authorizationService.assertPathId(tutorId, request.getId());
        authorizationService.assertManagerOwnsTutor(userDetails, tutorId);

        return ResponseEntity.ok(
            TutorResponse.fromDto(tutorService.edit(request.toDto()))
        );
    }

    @PutMapping("/{tutorId}/password")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> resetPassword(
        @Valid @RequestBody TutorPasswordResetRequest request,
        @PathVariable UUID tutorId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        authorizationService.assertManagerOwnsTutor(userDetails, tutorId);
        tutorService.resetPassword(tutorId, request.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{tutorId}/my-password")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<Void> changePassword(
        @Valid @RequestBody TutorPasswordChangeRequest request,
        @PathVariable UUID tutorId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        authorizationService.assertTutorSelf(userDetails, tutorId);
        tutorService.changePassword(tutorId, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{tutorId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> delete(
        @PathVariable UUID tutorId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        authorizationService.assertManagerOwnsTutor(userDetails, tutorId);

        tutorService.delete(tutorId);
        return ResponseEntity.noContent().build();
    }

}
