package in.rautkart.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Request / response payloads for signup, login and the current-user lookup. */
public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 6, max = 72) String password,
            @Pattern(regexp = "^$|^[0-9]{10}$", message = "phone must be 10 digits") String phone
    ) {
    }

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password
    ) {
    }

    public record AuthResponse(String token, UserResponse user) {
    }

    public record UserResponse(Long id, String name, String email, String phone, String role) {
    }

    public record ForgotPasswordRequest(
            @NotBlank @Email String email
    ) {
    }

    public record ResetPasswordRequest(
            @NotBlank String token,
            @NotBlank @Size(min = 6, max = 72) String newPassword
    ) {
    }

    public record ChangePasswordRequest(
            @NotBlank String currentPassword,
            @NotBlank @Size(min = 6, max = 72) String newPassword
    ) {
    }

    /**
     * Deliberately vague. The forgot-password endpoint returns the same message
     * whether or not the address exists, so it cannot be used to discover which
     * emails have accounts.
     *
     * resetToken is populated only when rautkart.password-reset.expose-token is
     * on, which is a local-development convenience because this project sends
     * no email. It is never set otherwise.
     */
    public record MessageResponse(String message, String resetToken) {
        public static MessageResponse of(String message) {
            return new MessageResponse(message, null);
        }
    }
}
