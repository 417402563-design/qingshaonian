package cn.edu.sdua.qingshaonian.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 心理知识库条目，作为大模型回答的检索增强上下文
 */
@Data
@Entity
@Table(name = "qs_knowledge_item")
public class KnowledgeItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(length = 1000)
    private String tags;

    @Column(length = 20)
    private String categoryCode;

    @Column(nullable = false, length = 20)
    private String status = "PUBLISHED";

    private Long viewCount = 0L;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();
}
