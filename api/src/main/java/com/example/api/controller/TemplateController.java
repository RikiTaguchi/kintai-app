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

import com.example.api.controller.request.template.TemplateEditRequest;
import com.example.api.controller.request.template.TemplateRegisterRequest;
import com.example.api.controller.response.TemplateResponse;
import com.example.api.security.CustomUserDetails;
import com.example.api.service.AuthorizationService;
import com.example.api.service.TemplateService;
import com.example.api.service.dto.template.TemplateDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
public class TemplateController {
    
    private final TemplateService templateService;
    private final AuthorizationService authorizationService;

    @GetMapping("/{tutorId}")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<List<TemplateResponse>> findAll(
        @PathVariable UUID tutorId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        authorizationService.assertTutorSelf(userDetails, tutorId);

        return ResponseEntity.ok(
            templateService.findAll(tutorId).stream()
                .map(TemplateResponse::fromDto)
                .toList()
        );

    }

    @GetMapping("/{tutorId}/{templateId}")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<TemplateResponse> find(
        @PathVariable UUID tutorId,
        @PathVariable UUID templateId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        authorizationService.assertTutorSelf(userDetails, tutorId);

        TemplateDto template = templateService.find(templateId);
        authorizationService.assertResourceBelongsToTutor(tutorId, template.getTutorId());

        return ResponseEntity.ok(
            TemplateResponse.fromDto(template)
        );

    }

    @PostMapping("/{tutorId}")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<TemplateResponse> register(
        @Valid @RequestBody TemplateRegisterRequest request,
        @PathVariable UUID tutorId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        authorizationService.assertPathId(tutorId, request.getTutorId());
        authorizationService.assertTutorSelf(userDetails, tutorId);

        return ResponseEntity.status(HttpStatus.CREATED).body(
            TemplateResponse.fromDto(templateService.register(request.toDto()))
        );

    }

    @PutMapping("/{tutorId}/{templateId}")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<TemplateResponse> edit(
        @Valid @RequestBody TemplateEditRequest request,
        @PathVariable UUID tutorId,
        @PathVariable UUID templateId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        authorizationService.assertPathId(tutorId, request.getTutorId());
        authorizationService.assertPathId(templateId, request.getId());
        authorizationService.assertTutorSelf(userDetails, tutorId);

        TemplateDto template = templateService.find(templateId);
        authorizationService.assertResourceBelongsToTutor(tutorId, template.getTutorId());

        return ResponseEntity.ok(
            TemplateResponse.fromDto(templateService.edit(request.toDto()))
        );

    }

    @DeleteMapping("/{tutorId}/{templateId}")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<Void> delete(
        @PathVariable UUID tutorId,
        @PathVariable UUID templateId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        authorizationService.assertTutorSelf(userDetails, tutorId);

        TemplateDto template = templateService.find(templateId);
        authorizationService.assertResourceBelongsToTutor(tutorId, template.getTutorId());

        templateService.delete(templateId);

        return ResponseEntity.noContent().build();

    }

}
