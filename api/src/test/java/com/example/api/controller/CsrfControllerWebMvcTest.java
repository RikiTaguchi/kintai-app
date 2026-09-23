package com.example.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.example.api.security.SecurityConfig;

@WebMvcTest(CsrfController.class)
@Import(SecurityConfig.class)
class CsrfControllerWebMvcTest {

    @Autowired private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/csrf: 200 + XSRF-TOKEN Cookie を返す（permitAll）")
    void getToken() throws Exception {
        mockMvc.perform(get("/api/csrf"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isString())
            .andExpect(jsonPath("$.headerName").value("X-XSRF-TOKEN"))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie()
                .exists("XSRF-TOKEN"));
    }
}
