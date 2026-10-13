package macieserafin.pl.helpdesk;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import macieserafin.pl.helpdesk.repository.UserRepository;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:admin-only;DB_CLOSE_DELAY=-1",
    "spring.flyway.enabled=false",
    "app.demo.seed.enabled=true",
    "app.demo.seed.admin-only=true",
    "app.demo.seed.require-custom-passwords=true",
    "app.demo.seed.admin-password=temporary-test-password-123",
    "app.demo.seed.user-password=",
    "app.demo.seed.agent-password="
})
class AdminOnlySeedTests {
    @Autowired UserRepository users;
    @Test void createsOnlyAdministrator() {
        assertEquals(1, users.count());
        assertEquals("admin", users.findAll().get(0).getLoginIdentifier());
        assertNotEquals("temporary-test-password-123", users.findAll().get(0).getPasswordHash());
    }
}
