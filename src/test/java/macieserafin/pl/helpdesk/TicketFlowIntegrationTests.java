package macieserafin.pl.helpdesk;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.attachments.storage-dir=target/test-attachments")
@AutoConfigureMockMvc
class TicketFlowIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldLoginWithFormEndpointAndUseSession() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "user")
                        .param("password", "user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user"))
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertThat(session).isNotNull();

        mockMvc.perform(get("/api/users/me")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user"));
    }

    @Test
    void shouldRejectInvalidFormLogin() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "user")
                        .param("password", "wrong-password"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid username or password"))
                .andExpect(jsonPath("$.path").value("/api/auth/login"))
                .andExpect(jsonPath("$.errors.length()").value(0));
    }

    @Test
    void shouldRegisterUserAndLoginWithFormEndpoint() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String username = "registered-" + suffix;
        String email = username + "@example.com";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "email": "%s",
                                  "password": "registered123",
                                  "profile": {
                                    "firstName": "Registered",
                                    "lastName": "User",
                                    "city": "Lodz"
                                  }
                                }
                                """.formatted(username, email)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.roles[0]").value("USER"))
                .andExpect(jsonPath("$.profile.firstName").value("Registered"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", username)
                        .param("password", "registered123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertThat(session).isNotNull();

        mockMvc.perform(get("/api/users/me")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void shouldRejectInvalidRequestBodies() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "ab",
                                  "email": "invalid-email",
                                  "password": "123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value("/api/auth/register"))
                .andExpect(jsonPath("$.errors.length()").value(3));

        mockMvc.perform(post("/api/tickets")
                        .with(httpBasic("user", "user123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": " ",
                                  "description": "",
                                  "category": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.length()").value(3));

        mockMvc.perform(patch("/api/admin/users/1/enabled")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "enabled": null
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors[0].field").value("enabled"));

        mockMvc.perform(post("/api/tickets")
                        .with(httpBasic("user", "user123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "x".repeat(151),
                                "description", "Opis",
                                "category", "Hardware"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors[0].field").value("title"));
    }

    @Test
    void shouldUseCommonErrorFormatForApiErrors() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Authentication is required"))
                .andExpect(jsonPath("$.path").value("/api/users/me"));

        mockMvc.perform(get("/api/admin/users")
                        .with(httpBasic("user", "user123")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Access denied"))
                .andExpect(jsonPath("$.path").value("/api/admin/users"));

        mockMvc.perform(get("/api/admin/users/999999")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User not found: 999999"))
                .andExpect(jsonPath("$.path").value("/api/admin/users/999999"));
    }

    @Test
    void shouldExposeTicketStatusAndPriorityContracts() throws Exception {
        mockMvc.perform(get("/api/tickets/statuses")
                        .with(httpBasic("user", "user123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(7))
                .andExpect(jsonPath("$[0]").value("OPEN"))
                .andExpect(jsonPath("$[6]").value("CANCELLED"));

        mockMvc.perform(get("/api/tickets/priorities")
                        .with(httpBasic("user", "user123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0]").value("UNASSIGNED"))
                .andExpect(jsonPath("$[4]").value("CRITICAL"));

        mockMvc.perform(get("/api/agent/tickets/assignable-priorities")
                        .with(httpBasic("agent", "agent123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0]").value("LOW"))
                .andExpect(jsonPath("$[3]").value("CRITICAL"));
    }

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
    void shouldManageUsersViaUserEndpoints() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String username = "managed-" + suffix;
        String email = username + "@example.com";

        String createdUserBody = mockMvc.perform(post("/api/admin/users")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "email": "%s",
                                  "password": "managed123",
                                  "roles": ["USER"],
                                  "profile": {
                                    "firstName": "Managed",
                                    "lastName": "User",
                                    "city": "Poznan"
                                  }
                                }
                                """.formatted(username, email)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.profile.firstName").value("Managed"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode createdUser = objectMapper.readTree(createdUserBody);
        Long userId = createdUser.get("id").asLong();
        assertThat(containsText(createdUser.get("roles"), "USER")).isTrue();

        mockMvc.perform(get("/api/admin/users/{id}", userId)
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username));

        String updatedUserBody = mockMvc.perform(patch("/api/admin/users/{id}", userId)
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "updated-%s",
                                  "roles": ["AGENT"],
                                  "profile": {
                                    "phoneNumber": "+48 600 700 800",
                                    "city": "Wroclaw"
                                  }
                                }
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("updated-" + email))
                .andExpect(jsonPath("$.profile.phoneNumber").value("+48 600 700 800"))
                .andExpect(jsonPath("$.profile.city").value("Wroclaw"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode updatedUser = objectMapper.readTree(updatedUserBody);
        assertThat(containsText(updatedUser.get("roles"), "AGENT")).isTrue();

        mockMvc.perform(get("/api/users/me")
                        .with(httpBasic(username, "managed123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username));

        mockMvc.perform(patch("/api/users/me/profile")
                        .with(httpBasic(username, "managed123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "streetAddress": "Testowa 1"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profile.streetAddress").value("Testowa 1"));

        mockMvc.perform(patch("/api/admin/users/{id}/enabled", userId)
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "enabled": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));
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
                .andExpect(jsonPath("$.priority").value("UNASSIGNED"))
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
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.priority").value("UNASSIGNED"));

        mockMvc.perform(patch("/api/agent/tickets/{id}/priority", ticketId)
                        .with(httpBasic("user", "user123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "priority": "CRITICAL"
                                }
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/agent/tickets/{id}/assign", ticketId)
                        .with(httpBasic("agent", "agent123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignedTo").value("agent"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        mockMvc.perform(patch("/api/agent/tickets/{id}/assign", ticketId)
                        .with(httpBasic("agent", "agent123")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ticket is already assigned to this agent"));

        mockMvc.perform(patch("/api/agent/tickets/{id}/priority", ticketId)
                        .with(httpBasic("agent", "agent123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "priority": "HIGH"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.priority").value("HIGH"));

        mockMvc.perform(post("/api/tickets/{id}/comments", ticketId)
                        .with(httpBasic("user", "user123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "To nie powinno byc wewnetrzne.",
                                  "internal": true
                                }
                                """))
                .andExpect(status().isForbidden());

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

        mockMvc.perform(get("/api/tickets/{id}", ticketId)
                        .with(httpBasic("user", "user123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("WAITING_FOR_USER"));

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

        mockMvc.perform(get("/api/tickets/{id}", ticketId)
                        .with(httpBasic("user", "user123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

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
                "PRIORITY_CHANGED",
                "COMMENT_ADDED",
                "TICKET_RESOLVED",
                "TICKET_CLOSED"
        );
    }

    @Test
    void shouldHandleTicketAttachments() throws Exception {
        String suffix = UUID.randomUUID().toString();
        String createdTicketBody = mockMvc.perform(post("/api/tickets")
                        .with(httpBasic("user", "user123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Attachment ticket %s",
                                  "description": "Ticket do testowania zalacznikow.",
                                  "category": "Konto"
                                }
                                """.formatted(suffix)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long ticketId = objectMapper.readTree(createdTicketBody).get("id").asLong();
        MockMultipartFile userFile = new MockMultipartFile(
                "file",
                "readme.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "hello attachment".getBytes(StandardCharsets.UTF_8)
        );

        String attachmentBody = mockMvc.perform(multipart("/api/tickets/{id}/attachments", ticketId)
                        .file(userFile)
                        .with(httpBasic("user", "user123")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ticketId").value(ticketId))
                .andExpect(jsonPath("$.commentId").doesNotExist())
                .andExpect(jsonPath("$.uploadedBy").value("user"))
                .andExpect(jsonPath("$.fileName").value("readme.txt"))
                .andExpect(jsonPath("$.contentType").value(MediaType.TEXT_PLAIN_VALUE))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long attachmentId = objectMapper.readTree(attachmentBody).get("id").asLong();

        mockMvc.perform(get("/api/tickets/{id}/attachments", ticketId)
                        .with(httpBasic("user", "user123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(attachmentId));

        mockMvc.perform(get("/api/tickets/{ticketId}/attachments/{attachmentId}", ticketId, attachmentId)
                        .with(httpBasic("user", "user123")))
                .andExpect(status().isOk())
                .andExpect(result -> assertThat(result.getResponse().getContentAsString())
                        .isEqualTo("hello attachment"));

        String internalCommentBody = mockMvc.perform(post("/api/tickets/{id}/comments", ticketId)
                        .with(httpBasic("agent", "agent123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "Komentarz wewnetrzny z zalacznikiem.",
                                  "internal": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long internalCommentId = objectMapper.readTree(internalCommentBody).get("id").asLong();
        MockMultipartFile internalFile = new MockMultipartFile(
                "file",
                "internal.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "internal note".getBytes(StandardCharsets.UTF_8)
        );

        String internalAttachmentBody = mockMvc.perform(multipart("/api/tickets/{id}/attachments", ticketId)
                        .file(internalFile)
                        .param("commentId", internalCommentId.toString())
                        .with(httpBasic("agent", "agent123")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.commentId").value(internalCommentId))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long internalAttachmentId = objectMapper.readTree(internalAttachmentBody).get("id").asLong();

        mockMvc.perform(get("/api/tickets/{id}/attachments", ticketId)
                        .with(httpBasic("user", "user123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/api/tickets/{id}/attachments", ticketId)
                        .with(httpBasic("agent", "agent123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/api/tickets/{ticketId}/attachments/{attachmentId}", ticketId, internalAttachmentId)
                        .with(httpBasic("user", "user123")))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/tickets/{ticketId}/attachments/{attachmentId}", ticketId, attachmentId)
                        .with(httpBasic("user", "user123")))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/tickets/{ticketId}/attachments/{attachmentId}", ticketId, internalAttachmentId)
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/tickets/{id}/attachments", ticketId)
                        .with(httpBasic("agent", "agent123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldManageTicketCategoriesAndRejectUnknownOrInactiveCategory() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String categoryName = "Test category " + suffix;
        String updatedCategoryName = "Updated category " + suffix;

        String createdCategoryBody = mockMvc.perform(post("/api/admin/categories")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "description": "Kategoria dodana przez admina"
                                }
                                """.formatted(categoryName)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(categoryName))
                .andExpect(jsonPath("$.active").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long categoryId = objectMapper.readTree(createdCategoryBody).get("id").asLong();

        mockMvc.perform(get("/api/categories")
                        .with(httpBasic("user", "user123")))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/admin/categories/{id}", categoryId)
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "description": "Zaktualizowany opis"
                                }
                                """.formatted(updatedCategoryName)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(updatedCategoryName))
                .andExpect(jsonPath("$.description").value("Zaktualizowany opis"));

        mockMvc.perform(post("/api/tickets")
                        .with(httpBasic("user", "user123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Unknown category %s",
                                  "description": "Tego ticketa nie mozna utworzyc.",
                                  "category": "Brak takiej kategorii %s"
                                }
                                """.formatted(suffix, suffix)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Category not found or inactive: Brak takiej kategorii " + suffix));

        mockMvc.perform(delete("/api/admin/categories/{id}", categoryId)
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/admin/categories/{id}", categoryId)
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(post("/api/tickets")
                        .with(httpBasic("user", "user123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Inactive category %s",
                                  "description": "Tego ticketa tez nie mozna utworzyc.",
                                  "category": "%s"
                                }
                                """.formatted(suffix, updatedCategoryName)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Category not found or inactive: " + updatedCategoryName));
    }

    @Test
    void shouldFilterAndPaginateTickets() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String categoryName = "Filter category " + suffix;
        String ticketTitle = "Filtered ticket " + suffix;
        String createdFrom = LocalDateTime.now().minusDays(1).withNano(0).toString();

        mockMvc.perform(post("/api/admin/categories")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s"
                                }
                                """.formatted(categoryName)))
                .andExpect(status().isCreated());

        String createdTicketBody = mockMvc.perform(post("/api/tickets")
                        .with(httpBasic("user", "user123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "%s",
                                  "description": "Ticket do testowania filtrow i paginacji.",
                                  "category": "%s"
                                }
                                """.formatted(ticketTitle, categoryName)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long ticketId = objectMapper.readTree(createdTicketBody).get("id").asLong();

        mockMvc.perform(patch("/api/agent/tickets/{id}/assign", ticketId)
                        .with(httpBasic("agent", "agent123")))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/agent/tickets/{id}/priority", ticketId)
                        .with(httpBasic("agent", "agent123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "priority": "HIGH"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/tickets")
                        .with(httpBasic("admin", "admin123"))
                        .param("status", "IN_PROGRESS")
                        .param("priority", "HIGH")
                        .param("category", categoryName)
                        .param("agent", "agent")
                        .param("createdFrom", createdFrom)
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(ticketId))
                .andExpect(jsonPath("$.content[0].category").value(categoryName))
                .andExpect(jsonPath("$.content[0].assignedTo").value("agent"))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(get("/api/tickets/me")
                        .with(httpBasic("user", "user123"))
                        .param("category", categoryName)
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(ticketId))
                .andExpect(jsonPath("$.size").value(1));
    }

    @Test
    void shouldExposeTicketQueueToAgentsOnly() throws Exception {
        mockMvc.perform(get("/api/agent/tickets")
                        .with(httpBasic("agent", "agent123")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/agent/tickets")
                        .with(httpBasic("user", "user123")))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRestrictTicketOwnershipToUsers() throws Exception {
        String ticketJson = """
                {
                  "title": "Role restricted ticket %s",
                  "description": "Ticket powinien nalezec tylko do klienta.",
                  "priority": "MEDIUM",
                  "category": "Role"
                }
                """;

        mockMvc.perform(post("/api/tickets")
                        .with(httpBasic("agent", "agent123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ticketJson.formatted(UUID.randomUUID())))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/tickets")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ticketJson.formatted(UUID.randomUUID())))
                .andExpect(status().isForbidden());

        String createdTicketBody = mockMvc.perform(post("/api/tickets")
                        .with(httpBasic("user", "user123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ticketJson.formatted(UUID.randomUUID())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.createdBy").value("user"))
                .andExpect(jsonPath("$.priority").value("UNASSIGNED"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long ticketId = objectMapper.readTree(createdTicketBody).get("id").asLong();

        mockMvc.perform(patch("/api/agent/tickets/{id}/assign", ticketId)
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/agent/tickets/{id}/assign", ticketId)
                        .with(httpBasic("agent", "agent123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignedTo").value("agent"));

        mockMvc.perform(patch("/api/agent/tickets/{id}/status", ticketId)
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "RESOLVED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"));

        mockMvc.perform(post("/api/tickets/{id}/comments", ticketId)
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "Notatka administracyjna.",
                                  "internal": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.author").value("admin"));
    }

    private boolean containsTicketId(String responseBody, Long ticketId) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode tickets = root.has("content") ? root.get("content") : root;

        for (JsonNode ticket : tickets) {
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

    private boolean containsText(JsonNode arrayNode, String value) {
        for (JsonNode item : arrayNode) {
            if (item.asText().equals(value)) {
                return true;
            }
        }

        return false;
    }

    private String actionTypes(String responseBody) throws Exception {
        StringBuilder actionTypes = new StringBuilder();
        for (JsonNode historyEntry : objectMapper.readTree(responseBody)) {
            actionTypes.append(historyEntry.get("actionType").asText()).append(",");
        }

        return actionTypes.toString();
    }
}
