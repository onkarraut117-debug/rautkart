package in.rautkart.service;

import in.rautkart.dto.AuthDtos;
import in.rautkart.entity.PasswordResetToken;
import in.rautkart.entity.User;
import in.rautkart.exception.ApiException;
import in.rautkart.repository.PasswordResetTokenRepository;
import in.rautkart.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Forgotten-password reset, and changing a password while signed in.
 *
 * There is no mail server in this project, so the reset link is written to the
 * application log. In a real deployment the same token would go out by email
 * and nothing else about this flow would change.
 */
@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    /** Same reply whether or not the address exists - see MessageResponse. */
    private static final String NEUTRAL_REPLY =
            "If that email has an account, a reset link is on its way.";

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final long ttlMinutes;
    private final int maxPerHour;
    private final boolean exposeToken;

    public PasswordResetService(UserRepository userRepository,
                                PasswordResetTokenRepository tokenRepository,
                                PasswordEncoder passwordEncoder,
                                @Value("${rautkart.password-reset.ttl-minutes}") long ttlMinutes,
                                @Value("${rautkart.password-reset.max-per-hour}") int maxPerHour,
                                @Value("${rautkart.password-reset.expose-token}") boolean exposeToken) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.ttlMinutes = ttlMinutes;
        this.maxPerHour = maxPerHour;
        this.exposeToken = exposeToken;
    }

    @Transactional
    public AuthDtos.MessageResponse requestReset(AuthDtos.ForgotPasswordRequest request) {
        Optional<User> maybeUser = userRepository.findByEmailIgnoreCase(request.email().trim());

        // No account: reply exactly as if there were one. Returning 404 here
        // would turn this endpoint into a way of enumerating customers.
        if (maybeUser.isEmpty()) {
            log.info("Password reset requested for an unknown address");
            return AuthDtos.MessageResponse.of(NEUTRAL_REPLY);
        }

        User user = maybeUser.get();
        Instant now = Instant.now();

        // Cheap throttle so the endpoint cannot be used to spam someone.
        long recent = tokenRepository.countByUserIdAndCreatedAtAfter(user.getId(), now.minus(1, ChronoUnit.HOURS));
        if (recent >= maxPerHour) {
            log.warn("Password reset throttled for user {}", user.getId());
            return AuthDtos.MessageResponse.of(NEUTRAL_REPLY);
        }

        // Any earlier link stops working the moment a new one is issued.
        tokenRepository.invalidateAllForUser(user.getId(), now);

        String rawToken = newToken();
        tokenRepository.save(PasswordResetToken.builder()
                .user(user)
                .tokenHash(sha256(rawToken))
                .expiresAt(now.plus(ttlMinutes, ChronoUnit.MINUTES))
                .build());

        // Stands in for sending an email.
        log.info("Password reset link for {}: /reset-password?token={}", user.getEmail(), rawToken);

        return new AuthDtos.MessageResponse(NEUTRAL_REPLY, exposeToken ? rawToken : null);
    }

    @Transactional
    public AuthDtos.MessageResponse reset(AuthDtos.ResetPasswordRequest request) {
        PasswordResetToken token = tokenRepository.findByTokenHash(sha256(request.token().trim()))
                .orElseThrow(() -> ApiException.badRequest("That reset link is not valid"));

        if (!token.isUsable()) {
            throw ApiException.badRequest("That reset link has expired or has already been used");
        }

        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        // Burn this token and any sibling still outstanding.
        tokenRepository.invalidateAllForUser(user.getId(), Instant.now());

        log.info("Password reset completed for user {}", user.getId());
        return AuthDtos.MessageResponse.of("Your password has been changed. You can sign in now.");
    }

    @Transactional
    public AuthDtos.MessageResponse changePassword(Long userId, AuthDtos.ChangePasswordRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> ApiException.notFound("User"));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Your current password is not correct");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw ApiException.badRequest("The new password must be different from the current one");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        // Someone changing their password may be locking an intruder out.
        tokenRepository.invalidateAllForUser(user.getId(), Instant.now());

        return AuthDtos.MessageResponse.of("Your password has been updated.");
    }

    /** 256 bits of randomness, URL-safe so it can go straight into a link. */
    private static String newToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required but unavailable", e);
        }
    }
}
