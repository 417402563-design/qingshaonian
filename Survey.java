package cn.edu.sdua.qingshaonian.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 自我探索 - 心理测评问卷（题目以 JSON 文本存储）
 */
@Data
@Entity
@Table(name = "qs_survey")
public class Survey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 300)
    private String description;

    /**
     * 题目 JSON：[{title, options:[{label, score}]}]
     */
    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String questions;

    /**
     * 结果解读 JSON：[{min, max, level, advice}]
     */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String interpretation;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
