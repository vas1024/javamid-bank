package javamid.accounts;

import javamid.accounts.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByLogin(String login);
  boolean existsByLogin(String login);
  Optional<User> findById(Long id);

  @Query("SELECT u FROM User u WHERE u.name LIKE %:name%")
  List<User> findByNameContaining(@Param("name") String name);
}

