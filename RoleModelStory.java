package cn.edu.sdua.qingshaonian.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 榜样故事栏 - 青少年励志榜样人物故事
 */
@Data
@Entity
@Table(name = "qs_role_model_story")
public class RoleModelStory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String tag;

    @Column(length = 300)
    private String summary;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    private Boolean featured = false;

    private Long viewCount = 0L;

    @Column(nullable = false, length = 20)
    private String status = "PUBLISHED";

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
