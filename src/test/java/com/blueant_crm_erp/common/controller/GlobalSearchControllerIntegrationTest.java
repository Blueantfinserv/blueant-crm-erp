package com.blueant_crm_erp.common.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class GlobalSearchControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "EMP001", roles = {"SALES"})
    public void testGlobalSearch_Authenticated() throws Exception {
        mockMvc.perform(get("/api/v1/search")
                .param("query", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.query").value("test"));
    }

    @Test
    public void testGlobalSearch_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/search")
                .param("query", "test"))
                .andExpect(status().isUnauthorized());
    }
}
