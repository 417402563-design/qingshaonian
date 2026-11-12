package cn.edu.sdua.qingshaonian.repository;

import cn.edu.sdua.qingshaonian.entity.InterventionCase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InterventionCaseRepository extends JpaRepository<InterventionCase, Long> {

    List<InterventionCase> findByCounselorId(Long counselorId);

    List<InterventionCase> findByStatus(String status);

    long countByStatus(String status);

    Optional<InterventionCase> findBySourceSessionId(Long sourceSessionId);
}
