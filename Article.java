package cn.edu.sdua.qingshaonian.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 成长驿站科普专栏文章
 */
@Data
@Entity
@Table(name = "qs_article")
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 50)
    private String category;

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
