package com.fabriciosanches.fichatecnica.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SwaggerControllerTest {

    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new SwaggerController()).build();

    @Test
    void swagger_DeveRedirecionarParaSwaggerUi() throws Exception {
        mockMvc.perform(get("/swagger"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/swagger-ui.html"));
    }

    @Test
    void docs_DeveRedirecionarParaSwaggerUi() throws Exception {
        mockMvc.perform(get("/docs"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/swagger-ui.html"));
    }
}

