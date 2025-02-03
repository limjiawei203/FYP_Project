package FYP;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PdfdatabaseRepository extends JpaRepository<Pdfdatabase, Integer> {
	Optional<Pdfdatabase> findById(Long id);
}