package cn.edu.sdua.qingshaonian.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 系统配置项（键值对，管理员端可在线维护）
 */
@Data
@Entity
@Table(name = "qs_system_config")
public class SystemConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String configKey;

    @Column(length = 1000)
    private String configValue;

    @Column(length = 200)
    private String description;
}
