package com.example.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.api.security.SecurityConfig;
import com.example.api.service.AuthorizationService;
import com.example.api.service.PayslipService;
import com.example.api.service.WorkService;
import com.example.api.service.dto.payslip.PayslipDto;
import com.example.api.service.dto.payslip.PayslipItemDto;

@WebMvcTest(PayslipController.class)
@Import(SecurityConfig.class)
class PayslipControllerWebMvcTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private PayslipService payslipService;
    @MockitoBean private WorkService workService;
    @MockitoBean private AuthorizationService authorizationService;

    private UUID tutorId;
    private UUID classroomId;

    @BeforeEach
    void setUp() {
        tutorId = UUID.randomUUID();
        classroomId = UUID.randomUUID();
    }

    @Test
    @DisplayName("GET /api/payslips/{tutorId}: 200 + PayslipResponse")
    void success() throws Exception {
        PayslipDto payslip = new PayslipDto();
        payslip.setTutorId(tutorId);
        payslip.setLessonPay(10000);
        payslip.setPeriodCount(3);
        payslip.setDailyAllowance(400);
        payslip.setOfficeWorkPay(1000);
        payslip.setTrainingAndStudyRoomDto(new PayslipItemDto(0, 0));
        payslip.setOutsideHoursWorkDto(new PayslipItemDto(0, 0));
        payslip.setOvertimePremiumDto(new PayslipItemDto(0, 0));
        payslip.setNightShiftPremiumDto(new PayslipItemDto(0, 0));
        payslip.setOtherPay(0);
        payslip.setTransportationFee(100);

        when(workService.findAll(tutorId, 2025, 9)).thenReturn(java.util.List.of());
        when(payslipService.create(any())).thenReturn(payslip);

        mockMvc.perform(get("/api/payslips/{tutorId}", tutorId)
                    .param("year", "2025").param("month", "9")
                    .with(org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.authentication(
                            SecurityUserTestHelper.auth(
                                SecurityUserTestHelper.tutorUser(tutorId, classroomId), "TUTOR"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tutorId").value(tutorId.toString()))
                .andExpect(jsonPath("$.lessonPay").value(10000))
                .andExpect(jsonPath("$.periodCount").value(3));

        verify(authorizationService).assertTutorSelf(any(), org.mockito.ArgumentMatchers.eq(tutorId));
        verify(payslipService).create(any());
    }

    @Test
    @DisplayName("他人の給与 → 403")
    void forbidden() throws Exception {
        org.mockito.Mockito.doThrow(
            new com.example.api.exception.AuthorizationFailedException("本人以外は見られません"))
            .when(authorizationService).assertTutorSelf(any(), org.mockito.ArgumentMatchers.eq(tutorId));

        mockMvc.perform(get("/api/payslips/{tutorId}", tutorId)
                .param("year", "2025").param("month", "9")
                .with(org.springframework.security.test.web.servlet.request
                    .SecurityMockMvcRequestPostProcessors.authentication(
                        SecurityUserTestHelper.auth(
                            SecurityUserTestHelper.tutorUser(tutorId, classroomId), "TUTOR"))))
            .andExpect(status().isForbidden());

        verify(payslipService, never()).create(any());
    }
}
