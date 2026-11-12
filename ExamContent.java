package cn.edu.sdua.qingshaonian.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 高考冲锋专栏内容（冲刺心理、减压技巧、备考策略等）
 */
@Data
@Entity
@Table(name = "qs_exam_content")
public class ExamContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 50)
    private String type;

    @Column(length = 300)
    private String summary;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    private Long viewCount = 0L;

    @Column(nullable = false, length = 20)
    private String status = "PUBLISHED";

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
