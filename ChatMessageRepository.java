package cn.edu.sdua.qingshaonian.repository;

import cn.edu.sdua.qingshaonian.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findBySessionIdOrderByIdAsc(Long sessionId);

    List<ChatMessage> findByRoleAndSessionIdOrderByIdAsc(String role, Long sessionId);

    Page<ChatMessage> findByRoleAndCreatedAtBetween(String role, LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<ChatMessage> findByRoleAndCreatedAtBetween(String role, LocalDateTime start, LocalDateTime end);

    long countByRole(String role);
}
