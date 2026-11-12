package cn.edu.sdua.qingshaonian.controller;

import cn.edu.sdua.qingshaonian.dto.ApiException;
import cn.edu.sdua.qingshaonian.dto.Dtos;
import cn.edu.sdua.qingshaonian.dto.Result;
import cn.edu.sdua.qingshaonian.entity.*;
import cn.edu.sdua.qingshaonian.repository.*;
import cn.edu.sdua.qingshaonian.service.AuthService;
import cn.edu.sdua.qingshaonian.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 管理员端接口（仅 ADMIN 角色，由 AuthInterceptor 保障）：
 * 账号与权限管理、知识库运营、内容管理、系统配置、服务状态监控、数据统计分析
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AuthService authService;
    private final StatsService statsService;
    private final UserRepository userRepository;
    private final KBCategoryRepository categoryRepository;
    private final KnowledgeItemRepository knowledgeRepository;
    private final RoleModelStoryRepository storyRepository;
    private final ExamContentRepository examRepository;
    private final ArticleRepository articleRepository;
    private final SystemConfigRepository configRepository;

    // ===================== 账号与权限管理 =====================

    @GetMapping("/users")
    public Result<List<User>> users(@RequestParam(required = false) String role) {
        List<User> list = role == null || role.isBlank()
                ? userRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                : userRepository.findByRoleOrderByCreatedAtDesc(role);
        list.forEach(Dtos::sanitize);
        return Result.ok(list);
    }

    @PutMapping("/users/{id}")
    public Result<User> updateUser(@PathVariable Long id, @RequestBody Dtos.UserStatusRequest req) {
        return Result.ok(authService.updateUserByAdmin(id, req));
    }

    @PostMapping("/counselors")
    public Result<User> createCounselor(@RequestBody Dtos.CounselorRequest req) {
        return Result.ok(authService.createCounselor(req));
    }

    // ===================== 知识库运营 =====================

    @GetMapping("/kb/categories")
    public Result<List<KBCategory>> categories() {
        List<KBCategory> list = categoryRepository.findAllByOrderBySortOrderAsc();
        list.forEach(c -> c.setEmbedding(null));
        return Result.ok(list);
    }

    @PostMapping("/kb/categories")
    public Result<KBCategory> saveCategory(@RequestBody KBCategory category) {
        category.setEmbedding(null);
        return Result.ok(categoryRepository.save(category));
    }

    @DeleteMapping("/kb/categories/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        categoryRepository.deleteById(id);
        return Result.ok();
    }

    @PostMapping("/kb/items")
    public Result<KnowledgeItem> saveItem(@RequestBody KnowledgeItem item) {
        if (item.getId() != null) {
            KnowledgeItem exist = knowledgeRepository.findById(item.getId())
                    .orElseThrow(() -> new ApiException("条目不存在"));
            item.setCreatedAt(exist.getCreatedAt());
            item.setViewCount(exist.getViewCount());
        }
        item.setUpdatedAt(LocalDateTime.now());
        return Result.ok(knowledgeRepository.save(item));
    }

    @DeleteMapping("/kb/items/{id}")
    public Result<Void> deleteItem(@PathVariable Long id) {
        knowledgeRepository.deleteById(id);
        return Result.ok();
    }

    // ===================== 内容管理（榜样故事 / 高考专栏 / 科普文章） =====================

    @PostMapping("/stories")
    public Result<RoleModelStory> saveStory(@RequestBody RoleModelStory story) {
        if (story.getId() != null) {
            RoleModelStory exist = storyRepository.findById(story.getId()).orElseThrow(() -> new ApiException("故事不存在"));
            story.setCreatedAt(exist.getCreatedAt());
            story.setViewCount(exist.getViewCount());
        }
        return Result.ok(storyRepository.save(story));
    }

    @DeleteMapping("/stories/{id}")
    public Result<Void> deleteStory(@PathVariable Long id) {
        storyRepository.deleteById(id);
        return Result.ok();
    }

    @PostMapping("/exam")
    public Result<ExamContent> saveExam(@RequestBody ExamContent content) {
        if (content.getId() != null) {
            ExamContent exist = examRepository.findById(content.getId()).orElseThrow(() -> new ApiException("内容不存在"));
            content.setCreatedAt(exist.getCreatedAt());
            content.setViewCount(exist.getViewCount());
        }
        return Result.ok(examRepository.save(content));
    }

    @DeleteMapping("/exam/{id}")
    public Result<Void> deleteExam(@PathVariable Long id) {
        examRepository.deleteById(id);
        return Result.ok();
    }

    @PostMapping("/articles")
    public Result<Article> saveArticle(@RequestBody Article article) {
        if (article.getId() != null) {
            Article exist = articleRepository.findById(article.getId()).orElseThrow(() -> new ApiException("文章不存在"));
            article.setCreatedAt(exist.getCreatedAt());
            article.setViewCount(exist.getViewCount());
        }
        return Result.ok(articleRepository.save(article));
    }

    @DeleteMapping("/articles/{id}")
    public Result<Void> deleteArticle(@PathVariable Long id) {
        articleRepository.deleteById(id);
        return Result.ok();
    }

    // ===================== 系统配置 =====================

    @GetMapping("/configs")
    public Result<List<SystemConfig>> configs() {
        return Result.ok(configRepository.findAll());
    }

    @PostMapping("/configs")
    @Transactional
    public Result<SystemConfig> saveConfig(@RequestBody SystemConfig config) {
        SystemConfig entity = config.getId() == null ? new SystemConfig() : configRepository.findById(config.getId())
                .orElseThrow(() -> new ApiException("配置不存在"));
        entity.setConfigKey(config.getConfigKey());
        entity.setConfigValue(config.getConfigValue());
        entity.setDescription(config.getDescription());
        return Result.ok(configRepository.save(entity));
    }

    @DeleteMapping("/configs/{id}")
    public Result<Void> deleteConfig(@PathVariable Long id) {
        configRepository.deleteById(id);
        return Result.ok();
    }

    // ===================== 问答记录可视化 / 热词分析 / 数据统计 / 服务监控 =====================

    @GetMapping("/qa-records")
    public Result<Map<String, Object>> qaRecords(@RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size,
                                                 @RequestParam(required = false) String start,
                                                 @RequestParam(required = false) String end,
                                                 @RequestParam(required = false) String category) {
        LocalDateTime s = start == null ? LocalDate.now().minusDays(30).atStartOfDay() : LocalDate.parse(start).atStartOfDay();
        LocalDateTime e = end == null ? LocalDateTime.now() : LocalDate.parse(end).atTime(23, 59, 59);
        return Result.ok(statsService.qaRecords(page, size, s, e, category));
    }

    @GetMapping("/analytics")
    public Result<Map<String, Object>> analytics(@RequestParam(required = false) String start,
                                                 @RequestParam(required = false) String end) {
        LocalDateTime s = start == null ? LocalDate.now().minusDays(14).atStartOfDay() : LocalDate.parse(start).atStartOfDay();
        LocalDateTime e = end == null ? LocalDateTime.now() : LocalDate.parse(end).atTime(23, 59, 59);
        return Result.ok(statsService.analytics(s, e));
    }

    @GetMapping("/stats/overview")
    public Result<Map<String, Object>> overview() {
        return Result.ok(statsService.overview());
    }

    @GetMapping("/monitor")
    public Result<Map<String, Object>> monitor() {
        return Result.ok(statsService.serviceStatus());
    }
}
