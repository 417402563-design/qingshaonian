package cn.edu.sdua.qingshaonian.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 咨询师案例干预记录
 */
@Data
@Entity
@Table(name = "qs_intervention_case")
public class InterventionCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long studentId;

    private Long counselorId;

    @Column(nullable = false, length = 20)
    private String level = "一般";

    @Column(length = 50)
    private String problemCategory;

    @Column(length = 300)
    private String summary;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String plan;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String progress;

    private Long sourceSessionId;

    /** OPEN / IN_PROGRESS / CLOSED */
    @Column(nullable = false, length = 20)
    private String status = "OPEN";

    private LocalDate nextFollowUpDate;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
