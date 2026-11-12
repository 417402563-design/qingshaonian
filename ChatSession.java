package cn.edu.sdua.qingshaonian.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 智能咨询会话（问答记录载体）
 */
@Data
@Entity
@Table(name = "qs_chat_session")
public class ChatSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(length = 100)
    private String title;

    @Column(length = 50)
    private String categoryCode;

    @Column(nullable = false, length = 20)
    private String riskLevel = "NONE";

    /** AI / ESCALATED（已转人工） / CLOSED */
    @Column(nullable = false, length = 20)
    private String status = "AI";

    @Column(length = 50)
    private String emotionTag;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();
}
