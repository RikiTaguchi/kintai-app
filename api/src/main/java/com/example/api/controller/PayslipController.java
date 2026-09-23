package com.example.api.controller;

import com.example.api.service.AuthorizationService;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.api.controller.response.PayslipResponse;
import com.example.api.security.CustomUserDetails;
import com.example.api.service.PayslipService;
import com.example.api.service.WorkService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payslips")
@RequiredArgsConstructor
public class PayslipController {

    private final AuthorizationService authorizationService;
    private final PayslipService payslipService;
    private final WorkService workService;
    
    @GetMapping("/{tutorId}")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<PayslipResponse> find(
        @RequestParam Integer year,
        @RequestParam Integer month,
        @PathVariable UUID tutorId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        authorizationService.assertTutorSelf(userDetails, tutorId);

        return ResponseEntity.ok(
            PayslipResponse.fromDto(
                payslipService.create(workService.findAll(tutorId, year, month))
            )
        );
    }

}
