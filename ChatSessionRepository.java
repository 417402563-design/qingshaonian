package cn.edu.sdua.qingshaonian.repository;

import cn.edu.sdua.qingshaonian.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    List<ChatSession> findByUserIdOrderByUpdatedAtDesc(Long userId);

    @Query("select s from ChatSession s where s.riskLevel <> :risk order by s.createdAt desc")
    List<ChatSession> findRiskySessions(@Param("risk") String risk);

    List<ChatSession> findByStatus(String status);

    List<ChatSession> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
