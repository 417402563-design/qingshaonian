package cn.edu.sdua.qingshaonian.repository;

import cn.edu.sdua.qingshaonian.entity.ExamContent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamContentRepository extends JpaRepository<ExamContent, Long> {

    Page<ExamContent> findByStatus(String status, Pageable pageable);

    Page<ExamContent> findByTypeAndStatus(String type, String status, Pageable pageable);
}
