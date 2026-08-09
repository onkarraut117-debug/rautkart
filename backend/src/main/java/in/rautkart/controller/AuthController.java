package in.rautkart.controller;

import in.rautkart.dto.AuthDtos;
import in.rautkart.security.AuthUser;
import in.rautkart.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthDtos.AuthResponse> register(@Valid @RequestBody AuthDtos.RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public AuthDtos.AuthResponse login(@Valid @RequestBody AuthDtos.LoginRequest request) {
        return authService.login(request, false);
    }

    /** Same credentials check as /login, but rejects non-admin accounts. */
    @PostMapping("/admin/login")
    public AuthDtos.AuthResponse adminLogin(@Valid @RequestBody AuthDtos.LoginRequest request) {
        return authService.login(request, true);
    }

    @GetMapping("/me")
    public AuthDtos.UserResponse me(@AuthenticationPrincipal AuthUser user) {
        return authService.currentUser(user.getId());
    }
}
