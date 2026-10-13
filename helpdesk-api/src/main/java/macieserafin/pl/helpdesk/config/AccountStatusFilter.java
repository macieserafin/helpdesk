package macieserafin.pl.helpdesk.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;
import macieserafin.pl.helpdesk.service.UserService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ResponseStatusException;

/** Sessions must not retain access after an administrator disables/demotes an account. */
public class AccountStatusFilter extends OncePerRequestFilter {
    private final UserService users;
    public AccountStatusFilter(UserService users) { this.users = users; }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            boolean valid;
            try {
                var user = users.findCurrentUser(auth.getName());
                Set<String> expected = user.getRoles().stream().map(role -> "ROLE_" + role).collect(Collectors.toSet());
                Set<String> actual = auth.getAuthorities().stream().map(role -> role.getAuthority())
                        .filter(role -> role.startsWith("ROLE_")).collect(Collectors.toSet());
                valid = user.isEnabled() && expected.equals(actual);
            } catch (ResponseStatusException exception) {
                if (exception.getStatusCode().value() != 404) throw exception;
                valid = false;
            }
            if (!valid) {
                SecurityContextHolder.clearContext();
                var session = request.getSession(false);
                if (session != null) session.invalidate();
                response.setStatus(401);
                response.setContentType("application/json");
                response.getWriter().write("{\"message\":\"Authentication is required\"}");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
