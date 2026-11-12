package cn.edu.sdua.qingshaonian.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统用户（学生 / 咨询师 / 管理员），不同角色使用各自的扩展字段
 */
@Data
@Entity
@Table(name = "qs_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 20)
    private String role;

    @Column(length = 50)
    private String nickname;

    @Column(length = 20)
    private String phone;

    @Column(length = 500)
    private String permissions;

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    // ---------- 学生扩展 ----------
    @Column(length = 100)
    private String school;

    @Column(length = 20)
    private String grade;

    @Column(length = 50)
    private String className;

    @Column(length = 50)
    private String psychProfileNo;

    // ---------- 咨询师扩展 ----------
    @Column(length = 50)
    private String title;

    @Column(length = 200)
    private String specialty;

    @Column(length = 50)
    private String licenseNo;

    @Column(length = 500)
    private String intro;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
