package macieserafin.pl.helpdesk.config;

import macieserafin.pl.helpdesk.dto.ApiErrorResponse;
import macieserafin.pl.helpdesk.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            UserService userService,
                                            ObjectMapper objectMapper) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                        .requestMatchers("/user", "/user/**").hasRole("USER")
                        .requestMatchers("/agent", "/agent/**").hasRole("AGENT")
                        .requestMatchers("/api/users", "/api/users/**").authenticated()
                        .requestMatchers("/api/tickets", "/api/tickets/**").authenticated()
                        .requestMatchers("/api/agent", "/api/agent/**").authenticated()
                        .requestMatchers("/api/admin", "/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/admin", "/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) ->
                                writeErrorResponse(response, objectMapper,
                                        ApiErrorResponse.of(HttpStatus.UNAUTHORIZED,
                                                "Authentication is required",
                                                request.getRequestURI())))
                        .accessDeniedHandler((request, response, exception) ->
                                writeErrorResponse(response, objectMapper,
                                        ApiErrorResponse.of(HttpStatus.FORBIDDEN,
                                                "Access denied",
                                                request.getRequestURI()))))
                .formLogin(form -> form
                        .loginProcessingUrl("/api/auth/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .successHandler((request, response, authentication) -> {
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            objectMapper.writeValue(response.getOutputStream(),
                                    userService.findCurrentUser(authentication.getName()));
                        })
                        .failureHandler((request, response, exception) -> {
                            writeErrorResponse(response, objectMapper,
                                    ApiErrorResponse.of(HttpStatus.UNAUTHORIZED,
                                            "Invalid username or password",
                                            request.getRequestURI()));
                        })
                        .permitAll())
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    private void writeErrorResponse(HttpServletResponse response,
                                    ObjectMapper objectMapper,
                                    ApiErrorResponse errorResponse) throws java.io.IOException {
        response.setStatus(errorResponse.status());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:*",
                "http://127.0.0.1:*"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
