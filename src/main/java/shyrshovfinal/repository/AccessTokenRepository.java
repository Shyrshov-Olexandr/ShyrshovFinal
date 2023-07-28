package shyrshovfinal.repository;

import shyrshovfinal.models.AccessToken;
import shyrshovfinal.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccessTokenRepository extends JpaRepository<AccessToken, Long> {
    AccessToken findByToken(String token);
    List<AccessToken> findAllByUserId(Long userId);
    AccessToken findByUser(Optional<User> user);
    AccessToken findByUserAndExpiresAtAfter(User user, LocalDateTime expiresAt);
}
