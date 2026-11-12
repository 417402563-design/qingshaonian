package cn.edu.sdua.qingshaonian.controller;

import cn.edu.sdua.qingshaonian.dto.ApiException;
import cn.edu.sdua.qingshaonian.dto.Result;
import cn.edu.sdua.qingshaonian.entity.Article;
import cn.edu.sdua.qingshaonian.entity.ExamContent;
import cn.edu.sdua.qingshaonian.entity.KBCategory;
import cn.edu.sdua.qingshaonian.entity.KnowledgeItem;
import cn.edu.sdua.qingshaonian.entity.RoleModelStory;
import cn.edu.sdua.qingshaonian.repository.ArticleRepository;
import cn.edu.sdua.qingshaonian.repository.ExamContentRepository;
import cn.edu.sdua.qingshaonian.repository.KBCategoryRepository;
import cn.edu.sdua.qingshaonian.repository.KnowledgeItemRepository;
import cn.edu.sdua.qingshaonian.repository.RoleModelStoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 内容浏览接口（登录后各角色均可访问）：
 * 榜样故事栏 / 高考冲锋专栏 / 成长驿站科普 / 心理知识库 (FAQ)
 */
@RestController
@RequestMapping("/api/content")
@RequiredArgsConstructor
public class ContentController {

    private final RoleModelStoryRepository storyRepository;
    private final ExamContentRepository examRepository;
    private final ArticleRepository articleRepository;
    private final KBCategoryRepository categoryRepository;
    private final KnowledgeItemRepository knowledgeRepository;

    @GetMapping("/stories")
    public Page<RoleModelStory> stories(@PageableDefault(size = 9, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return storyRepository.findByStatus("PUBLISHED", pageable);
    }

    @GetMapping("/stories/{id}")
    @Transactional
    public Result<RoleModelStory> story(@PathVariable Long id) {
        RoleModelStory story = storyRepository.findById(id).orElseThrow(() -> new ApiException("故事不存在"));
        story.setViewCount(story.getViewCount() + 1);
        storyRepository.save(story);
        return Result.ok(story);
    }

    @GetMapping("/exam")
    public Page<ExamContent> exam(@RequestParam(required = false) String type,
                                  @PageableDefault(size = 9, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return type == null || type.isBlank()
                ? examRepository.findByStatus("PUBLISHED", pageable)
                : examRepository.findByTypeAndStatus(type, "PUBLISHED", pageable);
    }

    @GetMapping("/exam/{id}")
    @Transactional
    public Result<ExamContent> examDetail(@PathVariable Long id) {
        ExamContent content = examRepository.findById(id).orElseThrow(() -> new ApiException("内容不存在"));
        content.setViewCount(content.getViewCount() + 1);
        examRepository.save(content);
        return Result.ok(content);
    }

    @GetMapping("/articles")
    public Page<Article> articles(@RequestParam(required = false) String category,
                                  @PageableDefault(size = 9, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return category == null || category.isBlank()
                ? articleRepository.findByStatus("PUBLISHED", pageable)
                : articleRepository.findByCategoryAndStatus(category, "PUBLISHED", pageable);
    }

    @GetMapping("/articles/{id}")
    @Transactional
    public Result<Article> article(@PathVariable Long id) {
        Article article = articleRepository.findById(id).orElseThrow(() -> new ApiException("文章不存在"));
        article.setViewCount(article.getViewCount() + 1);
        articleRepository.save(article);
        return Result.ok(article);
    }

    @GetMapping("/kb/categories")
    public Result<List<KBCategory>> categories() {
        List<KBCategory> list = categoryRepository.findAllByOrderBySortOrderAsc();
        list.forEach(c -> c.setEmbedding(null));
        return Result.ok(list);
    }

    @GetMapping("/kb/items")
    public Page<KnowledgeItem> kbItems(@RequestParam(required = false) String categoryCode,
                                       @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return categoryCode == null || categoryCode.isBlank()
                ? knowledgeRepository.findByStatusOrderByCreatedAtDesc("PUBLISHED", pageable)
                : knowledgeRepository.findByCategoryCodeAndStatusOrderByCreatedAtDesc(categoryCode, "PUBLISHED", pageable);
    }

    @GetMapping("/kb/items/{id}")
    @Transactional
    public Result<KnowledgeItem> kbItem(@PathVariable Long id) {
        KnowledgeItem item = knowledgeRepository.findById(id).orElseThrow(() -> new ApiException("条目不存在"));
        item.setViewCount(item.getViewCount() + 1);
        return Result.ok(knowledgeRepository.save(item));
    }
}
