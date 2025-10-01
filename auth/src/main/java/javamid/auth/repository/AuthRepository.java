package javamid.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import javamid.auth.model.Notification;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthRepository extends JpaRepository<Notification, Long> {

}
