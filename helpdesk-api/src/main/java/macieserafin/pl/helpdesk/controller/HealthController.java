package macieserafin.pl.helpdesk.controller;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    private final DataSource dataSource;
    public HealthController(DataSource dataSource) { this.dataSource = dataSource; }

    @GetMapping("/api/health")
    public ResponseEntity<Map<String, String>> health() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.setQueryTimeout(3);
            statement.execute("SELECT 1");
            return ResponseEntity.ok(Map.of("status", "UP"));
        } catch (Exception exception) {
            return ResponseEntity.status(503).body(Map.of("status", "DOWN"));
        }
    }
}
