package in.rautkart.controller;

import in.rautkart.dto.AuthDtos;
import in.rautkart.security.AuthUser;
import in.rautkart.service.AuthService;
import in.rautkart.service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    public AuthController(AuthService authService, PasswordResetService passwordResetService) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
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

    /**
     * Always answers 200 with the same message, whether or not the address has
     * an account. Anything else would let a caller discover who is registered.
     */
    @PostMapping("/forgot-password")
    public AuthDtos.MessageResponse forgotPassword(
            @Valid @RequestBody AuthDtos.ForgotPasswordRequest request) {
        return passwordResetService.requestReset(request);
    }

    @PostMapping("/reset-password")
    public AuthDtos.MessageResponse resetPassword(
            @Valid @RequestBody AuthDtos.ResetPasswordRequest request) {
        return passwordResetService.reset(request);
    }

    /** Changing a password while signed in - requires the current one. */
    @PostMapping("/change-password")
    public AuthDtos.MessageResponse changePassword(
            @AuthenticationPrincipal AuthUser user,
            @Valid @RequestBody AuthDtos.ChangePasswordRequest request) {
        return passwordResetService.changePassword(user.getId(), request);
    }
}
