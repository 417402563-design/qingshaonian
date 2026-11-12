package cn.edu.sdua.qingshaonian.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 心理知识库 - 常见问题分类
 */
@Data
@Entity
@Table(name = "qs_kb_category")
public class KBCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(length = 1000)
    private String keywords;

    /** 语义向量（JSON 数组文本），由嵌入接口生成，用于知识库检索增强 */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String embedding;

    private Integer sortOrder = 0;
}
