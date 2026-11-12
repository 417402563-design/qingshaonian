package cn.edu.sdua.qingshaonian.service;

import cn.edu.sdua.qingshaonian.entity.KBCategory;
import cn.edu.sdua.qingshaonian.entity.KnowledgeItem;
import cn.edu.sdua.qingshaonian.repository.KBCategoryRepository;
import cn.edu.sdua.qingshaonian.repository.KnowledgeItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 心理知识库检索：分类命中 + 条目选取，为大模型提供共情安抚的领域上下文
 */
@Service
@RequiredArgsConstructor
public class KnowledgeRetrievalService {

    private final KBCategoryRepository categoryRepository;
    private final KnowledgeItemRepository itemRepository;

    /**
     * 根据用户提问检索知识库，返回命中的分类与最相关条目
     */
    public Retrieval retrieve(String question) {
        List<KBCategory> categories = categoryRepository.findAll();
        List<KBCategory> matched = categories.stream()
                .map(c -> Map.entry(c, score(question, c)))
                .filter(e -> e.getValue() > 0)
                .sorted(Map.Entry.<KBCategory, Integer>comparingByValue().reversed())
                .limit(2)
                .map(Map.Entry::getKey)
                .toList();

        String categoryCode = matched.isEmpty() ? null : matched.get(0).getName();
        List<KnowledgeItem> items = new ArrayList<>();
        if (!matched.isEmpty()) {
            Set<String> names = matched.stream().map(KBCategory::getName).collect(Collectors.toSet());
            List<KnowledgeItem> pool = itemRepository.findByCategoryCodeInAndStatus(names, "PUBLISHED");
            items = pool.stream()
                    .map(i -> Map.entry(i, itemScore(question, i)))
                    .sorted(Map.Entry.<KnowledgeItem, Integer>comparingByValue().reversed())
                    .limit(3)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            if (items.isEmpty()) {
                items = pool.stream().limit(2).collect(Collectors.toList());
            }
        }
        return new Retrieval(categoryCode, matched, items);
    }

    /** 分类得分：分类名与关键词字典命中计数 + 二元组相似度加权 */
    private int score(String question, KBCategory c) {
        int s = 0;
        if (question.contains(c.getName())) {
            s += 10;
        }
        if (c.getKeywords() != null && !c.getKeywords().isBlank()) {
            s += Arrays.stream(c.getKeywords().split("[,，;；]"))
                    .filter(w -> !w.isBlank() && question.contains(w.trim()))
                    .count() * 2;
        }
        s = Math.max(s, (int) (bigramSimilarity(question, c.getName() + "," + c.getDescription()) * 5));
        return s;
    }

    private int itemScore(String question, KnowledgeItem item) {
        int s = 0;
        if (item.getTags() != null && !item.getTags().isBlank()) {
            s += Arrays.stream(item.getTags().split("[,，;；]"))
                    .filter(w -> !w.isBlank() && question.contains(w.trim()))
                    .count() * 3;
        }
        s += (int) (bigramSimilarity(question, item.getTitle()) * 8);
        s += (int) (bigramSimilarity(question, item.getContent() == null ? "" : item.getContent()) * 2);
        return s;
    }

    /** 中文二元组相似度（无需外部分词库的轻量语义匹配） */
    static double bigramSimilarity(String a, String b) {
        Set<String> sa = bigrams(a), sb = bigrams(b);
        if (sa.isEmpty() || sb.isEmpty()) {
            return 0;
        }
        Set<String> inter = new HashSet<>(sa);
        inter.retainAll(sb);
        return 2.0 * inter.size() / (sa.size() + sb.size());
    }

    private static Set<String> bigrams(String text) {
        Set<String> set = new HashSet<>();
        String clean = text == null ? "" : text.replaceAll("[^\\p{IsHan}a-zA-Z0-9]", "");
        for (int i = 0; i + 2 <= clean.length(); i++) {
            set.add(clean.substring(i, i + 2));
        }
        return set;
    }

    public record Retrieval(String categoryCode, List<KBCategory> categories, List<KnowledgeItem> items) {

        /** 拼装给大模型的知识库上下文 */
        public String context() {
            StringBuilder sb = new StringBuilder();
            for (KnowledgeItem item : items) {
                sb.append("【").append(item.getTitle()).append("】")
                        .append(item.getContent() == null ? "" : item.getContent().replace("\n", " "))
                        .append("\n");
            }
            return sb.toString();
        }
    }
}
