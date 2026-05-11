package macieserafin.pl.helpdesk;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TicketFlowIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldExposeTechnicalUserWithProfileData() throws Exception {
        String usersBody = mockMvc.perform(get("/api/admin/users")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode user = findUser(usersBody, "user");
        assertThat(user.get("email").asText()).isEqualTo("user@example.com");
        assertThat(user.get("enabled").asBoolean()).isTrue();
        assertThat(user.get("roles").get(0).asText()).isEqualTo("USER");
        assertThat(user.has("passwordHash")).isFalse();

        JsonNode profile = user.get("profile");
        assertThat(profile.get("firstName").asText()).isEqualTo("Jan");
        assertThat(profile.get("lastName").asText()).isEqualTo("Kowalski");
        assertThat(profile.get("phoneNumber").asText()).isEqualTo("+48 500 100 100");
//        assertThat(profile.has("department")).isFalse();
//        assertThat(profile.has("position")).isFalse();
//        assertThat(profile.has("placeOfResidence")).isFalse();
        assertThat(profile.get("city").asText()).isEqualTo("Warszawa");
        assertThat(profile.get("streetAddress").asText()).isEqualTo("Marszalkowska 10");
        assertThat(profile.get("postalCode").asText()).isEqualTo("00-001");
    }

    @Test
    void shouldHandleMainTicketFlow() throws Exception {
        String ticketTitle = "Flow ticket " + UUID.randomUUID();

        String createTicketJson = """
                {
                  "title": "%s",
                  "description": "Nie dziala logowanie w panelu klienta.",
                  "priority": "HIGH",
                  "category": "Konto"
                }
                """.formatted(ticketTitle);

        String createdTicketBody = mockMvc.perform(post("/api/tickets")
                        .with(httpBasic("user", "user123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createTicketJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.createdBy").value("user"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long ticketId = objectMapper.readTree(createdTicketBody).get("id").asLong();

        String myTicketsBody = mockMvc.perform(get("/api/tickets/me")
                        .with(httpBasic("user", "user123")))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(containsTicketId(myTicketsBody, ticketId)).isTrue();

        mockMvc.perform(get("/api/tickets/{id}", ticketId)
                        .with(httpBasic("user", "user123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ticketId))
                .andExpect(jsonPath("$.status").value("OPEN"));

        mockMvc.perform(patch("/api/agent/tickets/{id}/assign", ticketId)
                        .with(httpBasic("agent", "agent123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignedTo").value("agent"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        mockMvc.perform(post("/api/tickets/{id}/comments", ticketId)
                        .with(httpBasic("user", "user123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "Dalej nie moge sie zalogowac.",
                                  "internal": false
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.author").value("user"));

        mockMvc.perform(post("/api/tickets/{id}/comments", ticketId)
                        .with(httpBasic("agent", "agent123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "Sprawdzam reset hasla i logi.",
                                  "internal": false
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.author").value("agent"));

        mockMvc.perform(get("/api/tickets/{id}/comments", ticketId)
                        .with(httpBasic("user", "user123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(patch("/api/agent/tickets/{id}/status", ticketId)
                        .with(httpBasic("agent", "agent123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "RESOLVED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"));

        mockMvc.perform(patch("/api/agent/tickets/{id}/status", ticketId)
                        .with(httpBasic("user", "user123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "CLOSED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));

        String historyBody = mockMvc.perform(get("/api/tickets/{id}/history", ticketId)
                        .with(httpBasic("user", "user123")))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(actionTypes(historyBody)).contains(
                "TICKET_CREATED",
                "ASSIGNED_CHANGED",
                "COMMENT_ADDED",
                "TICKET_RESOLVED",
                "TICKET_CLOSED"
        );
    }

    private boolean containsTicketId(String responseBody, Long ticketId) throws Exception {
        for (JsonNode ticket : objectMapper.readTree(responseBody)) {
            if (ticket.get("id").asLong() == ticketId) {
                return true;
            }
        }

        return false;
    }

    private JsonNode findUser(String responseBody, String username) throws Exception {
        for (JsonNode user : objectMapper.readTree(responseBody)) {
            if (user.get("username").asText().equals(username)) {
                return user;
            }
        }

        throw new AssertionError("User not found in response: " + username);
    }

    private String actionTypes(String responseBody) throws Exception {
        StringBuilder actionTypes = new StringBuilder();
        for (JsonNode historyEntry : objectMapper.readTree(responseBody)) {
            actionTypes.append(historyEntry.get("actionType").asText()).append(",");
        }

        return actionTypes.toString();
    }
}
