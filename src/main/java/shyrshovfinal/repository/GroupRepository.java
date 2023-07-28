package shyrshovfinal.repository;

import shyrshovfinal.models.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<Group,String> {
    boolean existsByName(String name);
    Group findByName(String name);
}
