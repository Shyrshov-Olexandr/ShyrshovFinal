package shyrshovfinal.repository;

import shyrshovfinal.models.RefreshToken;
import shyrshovfinal.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface
RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    RefreshToken findByToken(String token);

    List<RefreshToken> findAllByUserId(Long userId);
    RefreshToken findByUserAndExpiresAtAfter(User user, LocalDateTime expiresAt);
}
