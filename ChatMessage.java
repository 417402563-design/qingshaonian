package cn.edu.sdua.qingshaonian.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话消息（学生提问 + 大模型/知识库应答）
 */
@Data
@Entity
@Table(name = "qs_chat_message")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long sessionId;

    /** user / assistant */
    @Column(nullable = false, length = 20)
    private String role;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(length = 50)
    private String categoryCode;

    @Column(length = 20)
    private String emotionTag;

    private Boolean riskFlag = false;

    /** DeepSeek / KNOWLEDGE_BASE */
    @Column(length = 30)
    private String source;

    private Integer tokens;

    private Integer latencyMs;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
