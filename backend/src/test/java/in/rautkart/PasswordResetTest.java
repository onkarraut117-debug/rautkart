package in.rautkart;

import in.rautkart.entity.PasswordResetToken;
import in.rautkart.repository.PasswordResetTokenRepository;
import in.rautkart.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The reset flow. expose-token is switched on so the test can read the token
 * out of the response instead of scraping it from the log.
 */
@TestPropertySource(properties = "rautkart.password-reset.expose-token=true")
class PasswordResetTest extends AbstractIntegrationTest {

    private static final String EMAIL = "customer@rautkart.in";
    private static final String ORIGINAL = "customer123";

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void clearTokensAndRestorePassword() {
        tokenRepository.deleteAll();
        // Earlier tests in this class change the password; put it back so each
        // one starts from the seeded state.
        var user = userRepository.findByEmailIgnoreCase(EMAIL).orElseThrow();
        user.setPasswordHash(passwordEncoder.encode(ORIGINAL));
        userRepository.save(user);
    }

    private String requestResetToken() throws Exception {
        String body = mvc.perform(post("/api/auth/forgot-password")
                        .contentType("application/json")
                        .content("{\"email\":\"%s\"}".formatted(EMAIL)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(body).get("resetToken").asText();
    }

    private void expectLogin(String password, boolean shouldWork) throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(EMAIL, password)))
                .andExpect(shouldWork ? status().isOk() : status().isUnauthorized());
    }

    @Test
    @DisplayName("an unknown email gets the same answer as a real one")
    void doesNotRevealWhetherAnAccountExists() throws Exception {
        String known = mvc.perform(post("/api/auth/forgot-password")
                        .contentType("application/json")
                        .content("{\"email\":\"%s\"}".formatted(EMAIL)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String unknown = mvc.perform(post("/api/auth/forgot-password")
                        .contentType("application/json")
                        .content("{\"email\":\"nobody@example.com\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(json.readTree(known).get("message").asText())
                .isEqualTo(json.readTree(unknown).get("message").asText());

        // ...and no token was minted for the address that does not exist.
        assertThat(json.readTree(unknown).get("resetToken").isNull()).isTrue();
    }

    @Test
    @DisplayName("a valid token changes the password")
    void resetChangesThePassword() throws Exception {
        String token = requestResetToken();

        mvc.perform(post("/api/auth/reset-password")
                        .contentType("application/json")
                        .content("{\"token\":\"%s\",\"newPassword\":\"brand-new-pass\"}".formatted(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Your password has been changed. You can sign in now."));

        expectLogin("brand-new-pass", true);
        expectLogin(ORIGINAL, false);
    }

    @Test
    @DisplayName("a token cannot be used twice")
    void tokenIsSingleUse() throws Exception {
        String token = requestResetToken();

        mvc.perform(post("/api/auth/reset-password")
                        .contentType("application/json")
                        .content("{\"token\":\"%s\",\"newPassword\":\"first-change\"}".formatted(token)))
                .andExpect(status().isOk());

        mvc.perform(post("/api/auth/reset-password")
                        .contentType("application/json")
                        .content("{\"token\":\"%s\",\"newPassword\":\"second-change\"}".formatted(token)))
                .andExpect(status().isBadRequest());

        expectLogin("first-change", true);
        expectLogin("second-change", false);
    }

    @Test
    @DisplayName("an expired token is refused")
    void expiredTokenIsRefused() throws Exception {
        String token = requestResetToken();

        PasswordResetToken row = tokenRepository.findByTokenHash(sha256(token)).orElseThrow();
        row.setExpiresAt(Instant.now().minus(1, ChronoUnit.MINUTES));
        tokenRepository.save(row);

        mvc.perform(post("/api/auth/reset-password")
                        .contentType("application/json")
                        .content("{\"token\":\"%s\",\"newPassword\":\"too-late\"}".formatted(token)))
                .andExpect(status().isBadRequest());

        expectLogin(ORIGINAL, true);
    }

    @Test
    @DisplayName("issuing a new token invalidates the previous one")
    void newTokenInvalidatesTheOld() throws Exception {
        String first = requestResetToken();
        String second = requestResetToken();

        mvc.perform(post("/api/auth/reset-password")
                        .contentType("application/json")
                        .content("{\"token\":\"%s\",\"newPassword\":\"via-old-link\"}".formatted(first)))
                .andExpect(status().isBadRequest());

        mvc.perform(post("/api/auth/reset-password")
                        .contentType("application/json")
                        .content("{\"token\":\"%s\",\"newPassword\":\"via-new-link\"}".formatted(second)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("a made-up token is refused")
    void garbageTokenIsRefused() throws Exception {
        mvc.perform(post("/api/auth/reset-password")
                        .contentType("application/json")
                        .content("{\"token\":\"not-a-real-token\",\"newPassword\":\"whatever123\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("the raw token is never stored, only its digest")
    void onlyTheDigestIsStored() throws Exception {
        String token = requestResetToken();

        assertThat(tokenRepository.findAll())
                .allSatisfy(row -> assertThat(row.getTokenHash()).isNotEqualTo(token));
        assertThat(tokenRepository.findByTokenHash(sha256(token))).isPresent();
    }

    @Test
    @DisplayName("changing a password requires the current one")
    void changePasswordNeedsTheCurrentPassword() throws Exception {
        String token = customerToken();

        mvc.perform(post("/api/auth/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("{\"currentPassword\":\"wrong-one\",\"newPassword\":\"does-not-matter\"}"))
                .andExpect(status().isUnauthorized());

        mvc.perform(post("/api/auth/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("{\"currentPassword\":\"%s\",\"newPassword\":\"a-better-password\"}".formatted(ORIGINAL)))
                .andExpect(status().isOk());

        expectLogin("a-better-password", true);
        expectLogin(ORIGINAL, false);
    }

    @Test
    @DisplayName("the new password must actually be different")
    void newPasswordMustDiffer() throws Exception {
        mvc.perform(post("/api/auth/change-password")
                        .header("Authorization", "Bearer " + customerToken())
                        .contentType("application/json")
                        .content("{\"currentPassword\":\"%s\",\"newPassword\":\"%s\"}".formatted(ORIGINAL, ORIGINAL)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("changing a password kills any outstanding reset link")
    void changingPasswordInvalidatesResetTokens() throws Exception {
        String resetToken = requestResetToken();

        mvc.perform(post("/api/auth/change-password")
                        .header("Authorization", "Bearer " + customerToken())
                        .contentType("application/json")
                        .content("{\"currentPassword\":\"%s\",\"newPassword\":\"locked-you-out\"}".formatted(ORIGINAL)))
                .andExpect(status().isOk());

        // An attacker who had requested a reset cannot now use it.
        mvc.perform(post("/api/auth/reset-password")
                        .contentType("application/json")
                        .content("{\"token\":\"%s\",\"newPassword\":\"attacker-pass\"}".formatted(resetToken)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("reset and forgot-password need no authentication")
    void resetEndpointsArePublic() throws Exception {
        mvc.perform(post("/api/auth/forgot-password")
                        .contentType("application/json")
                        .content("{\"email\":\"%s\"}".formatted(EMAIL)))
                .andExpect(status().isOk());
    }

    private static String sha256(String value) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
    }
}
