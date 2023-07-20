package internfinalproject.repository;

import internfinalproject.models.Group;
import internfinalproject.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group,String> {
    boolean existsByName(String name);
    Group findByName(String name);
}
