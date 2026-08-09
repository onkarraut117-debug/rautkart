package in.rautkart.repository;

import in.rautkart.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    /**
     * Burns every outstanding token for a user. Called when one is redeemed and
     * when a new one is issued, so an old link in an old email cannot be used.
     */
    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.usedAt = :now WHERE t.user.id = :userId AND t.usedAt IS NULL")
    void invalidateAllForUser(@Param("userId") Long userId, @Param("now") Instant now);

    long countByUserIdAndCreatedAtAfter(Long userId, Instant since);
}
