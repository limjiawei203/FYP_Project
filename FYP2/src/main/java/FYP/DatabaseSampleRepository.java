package FYP;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseSampleRepository extends JpaRepository<Exceldatabase, Integer> {
	Optional<Exceldatabase> findById(Long id);
    // You can leave this empty, as we'll handle the finding logic in the service
}