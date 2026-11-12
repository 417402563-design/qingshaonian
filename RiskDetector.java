package cn.edu.sdua.qingshaonian.service;

import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 危机词与情绪识别服务：实现"双路径应答"中的风险路径判断
 */
@Service
public class RiskDetector {

    /** 一级危机词：命中即标记 HIGH，触发咨询师介入路径 */
    private static final List<String> HIGH_RISK_WORDS = List.of(
            "自杀", "不想活", "活不下去", "去死", "死了算了", "割腕", "自残", "伤害自己",
            "一了百了", "结束生命", "跳楼", "吞药");

    /** 二级预警词：标记 MEDIUM，进入咨询师关注列表 */
    private static final List<String> MEDIUM_RISK_WORDS = List.of(
            "撑不下去", "崩溃", "绝望", "没意思", "活着好累", "窒息", "恐惧", "颤抖",
            "被害", "自伤", "抑郁", "焦虑发作");

    /** 情绪标签词典 */
    private static final Map<String, List<String>> EMOTION_WORDS = Map.of(
            "焦虑", List.of("焦虑", "紧张", "担心", "害怕", "睡不着", "失眠", "心慌"),
            "低落", List.of("难过", "伤心", "想哭", "低落", "委屈", "不开心", "郁闷"),
            "愤怒", List.of("生气", "愤怒", "烦死", "讨厌", "吵架", "忍不住发火"),
            "孤独", List.of("孤独", "没人懂", "朋友", "孤立", "被排挤", "合群"),
            "压力", List.of("压力", "考试", "成绩", "排名", "作业", "升学", "高考", "中考"),
            "困惑", List.of("迷茫", "不知道", "怎么办", "选择", "困惑"));

    public Detection detect(String text) {
        String t = text == null ? "" : text;
        Set<String> hit = new LinkedHashSet<>();
        for (String w : HIGH_RISK_WORDS) {
            if (t.contains(w)) {
                hit.add(w);
            }
        }
        if (!hit.isEmpty()) {
            return new Detection("HIGH", emotion(t), List.copyOf(hit));
        }
        for (String w : MEDIUM_RISK_WORDS) {
            if (t.contains(w)) {
                hit.add(w);
            }
        }
        if (!hit.isEmpty()) {
            return new Detection("MEDIUM", emotion(t), List.copyOf(hit));
        }
        return new Detection("NONE", emotion(t), List.of());
    }

    private String emotion(String t) {
        String best = "平静";
        int bestCount = 0;
        for (Map.Entry<String, List<String>> e : EMOTION_WORDS.entrySet()) {
            int c = 0;
            for (String w : e.getValue()) {
                if (t.contains(w)) {
                    c++;
                }
            }
            if (c > bestCount) {
                bestCount = c;
                best = e.getKey();
            }
        }
        return best;
    }

    public record Detection(String riskLevel, String emotionTag, List<String> hitWords) {
    }
}
