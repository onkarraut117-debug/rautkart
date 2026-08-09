package in.rautkart.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * A single-use, time-limited ticket for resetting a forgotten password.
 *
 * Only a SHA-256 digest of the token is stored, never the token itself. If the
 * database leaks, the rows are useless: an attacker would have to invert the
 * digest to produce a usable reset link. A fast hash is the right choice here
 * (unlike for passwords) because the token is 256 bits of randomness, so there
 * is nothing to guess and nothing to slow down.
 */
@Entity
@Table(name = "password_reset_tokens",
       indexes = @Index(name = "idx_prt_token_hash", columnList = "token_hash"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    /** Set the moment it is redeemed, which is what makes it single-use. */
    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public boolean isUsable() {
        return usedAt == null && expiresAt.isAfter(Instant.now());
    }
}
