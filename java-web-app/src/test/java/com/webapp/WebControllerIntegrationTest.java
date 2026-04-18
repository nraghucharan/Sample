package com.webapp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class WebControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void homePage_isPublic() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(view().name("home"));
    }

    @Test
    void loginPage_isPublic() throws Exception {
        mockMvc.perform(get("/login"))
            .andExpect(status().isOk())
            .andExpect(view().name("login"));
    }

    @Test
    void registerPage_isPublic() throws Exception {
        mockMvc.perform(get("/register"))
            .andExpect(status().isOk())
            .andExpect(view().name("register"));
    }

    @Test
    void dashboard_redirectsToLoginWhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/dashboard"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void registerUser_validInput_redirectsToLogin() throws Exception {
        mockMvc.perform(post("/register")
                .with(csrf())
                .param("username", "newuser")
                .param("email", "newuser@example.com")
                .param("password", "password123"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));
    }

    @Test
    void registerUser_blankUsername_staysOnRegisterPage() throws Exception {
        mockMvc.perform(post("/register")
                .with(csrf())
                .param("username", "")
                .param("email", "test@example.com")
                .param("password", "password123"))
            .andExpect(status().isOk())
            .andExpect(view().name("register"));
    }
}
