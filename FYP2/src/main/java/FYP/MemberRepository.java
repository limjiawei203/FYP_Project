package FYP;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Integer> {
    Member findByNric(String nric); // Use NRIC for authentication
}
