package shyrshovfinal.repository;

import shyrshovfinal.models.Paid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaidRepository extends JpaRepository<Paid,Long> {

    Page<Paid> findAll(Specification<Paid> spec, Pageable pageable);
    List<Paid> findByUserEmail(String email);


}
