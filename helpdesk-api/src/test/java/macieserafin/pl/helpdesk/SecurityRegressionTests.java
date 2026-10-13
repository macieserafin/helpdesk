package macieserafin.pl.helpdesk;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

@SpringBootTest(properties = {"app.security.basic-auth-enabled=true", "app.security.min-password-length=12"})
@AutoConfigureMockMvc
class SecurityRegressionTests {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired macieserafin.pl.helpdesk.repository.UserRepository users;

    @Test void loginWithoutTokenIsRejected() throws Exception {
        mvc.perform(post("/api/auth/login").param("loginIdentifier", "admin").param("password", "admin123"))
                .andExpect(status().isForbidden());
    }

    @Test void browserTokenWorksAcrossLoginAndLogout() throws Exception {
        var tokenResult = mvc.perform(get("/api/auth/csrf")).andExpect(status().isOk()).andReturn();
        var token = mapper.readTree(tokenResult.getResponse().getContentAsString());
        var session = (MockHttpSession) tokenResult.getRequest().getSession(false);
        var login = mvc.perform(post("/api/auth/login").session(session)
                .header(token.get("headerName").asText(), token.get("token").asText())
                .param("loginIdentifier", "admin").param("password", "admin123"))
                .andExpect(status().isOk()).andReturn();
        session = (MockHttpSession) login.getRequest().getSession(false);
        var refreshed = mvc.perform(get("/api/auth/csrf").session(session)).andReturn();
        token = mapper.readTree(refreshed.getResponse().getContentAsString());
        mvc.perform(post("/api/auth/logout").session(session)
                .header(token.get("headerName").asText(), token.get("token").asText()))
                .andExpect(status().isNoContent());
    }

    @Test void userCannotReadAdminData() throws Exception {
        mvc.perform(get("/api/admin/users").with(httpBasic("user", "user123")))
                .andExpect(status().isForbidden());
    }

    @Test void adminCannotCreateUnknownRole() throws Exception {
        mvc.perform(post("/api/admin/users").with(httpBasic("admin", "admin123")).with(csrf())
                .contentType("application/json")
                .content("{\"loginIdentifier\":\"invalid-role\",\"email\":\"invalid-role@example.com\",\"password\":\"test-password-123\",\"roles\":[\"SUPERUSER\"]}"))
                .andExpect(status().isBadRequest());
    }

    @Test void rejectsWeakNewPasswords() throws Exception {
        mvc.perform(post("/api/auth/register").with(csrf()).contentType("application/json")
                .content("{\"loginIdentifier\":\"weak-password\",\"email\":\"weak@example.com\",\"password\":\"short12\",\"confirmPassword\":\"short12\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test void disablingAccountRevokesExistingSessionOnNextRequest() throws Exception {
        var login = mvc.perform(post("/api/auth/login").with(csrf())
                .param("loginIdentifier", "user").param("password", "user123"))
                .andExpect(status().isOk()).andReturn();
        var session = (MockHttpSession) login.getRequest().getSession(false);
        var user = users.findByLoginIdentifier("user").orElseThrow();
        user.setEnabled(false);
        users.save(user);
        try {
            mvc.perform(get("/api/users/me").session(session)).andExpect(status().isUnauthorized());
        } finally {
            user.setEnabled(true);
            users.save(user);
        }
    }
}
