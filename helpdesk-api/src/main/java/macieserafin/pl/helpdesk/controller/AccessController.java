package macieserafin.pl.helpdesk.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccessController {

    @GetMapping("/")
    String home() {
        return "Helpdesk API is running. Public endpoint.";
    }

    @GetMapping("/user")
    String userArea() {
        return "Hello USER";
    }

    @GetMapping("/agent")
    String agentArea() {
        return "Hello AGENT";
    }

    @GetMapping("/admin")
    String adminArea() {
        return "Hello ADMIN";
    }
}
