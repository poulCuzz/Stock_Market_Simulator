package pl.coderslab.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.coderslab.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    void saveUser(User user);
}
