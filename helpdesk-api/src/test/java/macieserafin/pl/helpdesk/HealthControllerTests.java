package macieserafin.pl.helpdesk;

import macieserafin.pl.helpdesk.controller.HealthController;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class HealthControllerTests {
    @Test
    void returnsUnavailableWithoutDatabaseAndDoesNotExposeError() throws Exception {
        DataSource source = mock(DataSource.class);
        when(source.getConnection()).thenThrow(new SQLException("secret connection details"));
        var result = new HealthController(source).health();
        assertEquals(503, result.getStatusCode().value());
        assertEquals(java.util.Map.of("status", "DOWN"), result.getBody());
    }

    @Test
    void checksDatabaseAndClosesResources() throws Exception {
        DataSource source = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        when(source.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        assertEquals(200, new HealthController(source).health().getStatusCode().value());
        verify(statement).execute("SELECT 1");
        verify(statement).close();
        verify(connection).close();
    }
}
