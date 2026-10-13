package macieserafin.pl.helpdesk;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("prod")
@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:production-schema;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=validate",
    "spring.flyway.enabled=true",
    "app.demo.seed.enabled=false"
})
class ProductionSchemaTests {
    @Test void migrationMatchesJpaModel() { }
}
