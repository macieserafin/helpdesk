package macieserafin.pl.helpdesk;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.attachments.storage-dir=target/test-attachments",
        "app.security.basic-auth-enabled=false"
})
@AutoConfigureMockMvc
class BasicAuthDisabledIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldDisableBasicAuthAndKeepSessionLoginAvailable() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .with(httpBasic("user", "user123")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication is required"));

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("loginIdentifier", "user")
                        .param("password", "user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loginIdentifier").value("user"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertThat(session).isNotNull();

        mockMvc.perform(get("/api/users/me")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loginIdentifier").value("user"));
    }
}
