package cn.edu.sdua.qingshaonian.service;

import cn.edu.sdua.qingshaonian.dto.ApiException;
import cn.edu.sdua.qingshaonian.entity.Survey;
import cn.edu.sdua.qingshaonian.repository.SurveyRepository;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自我探索：心理测评问卷与结果解读
 */
@Service
@RequiredArgsConstructor
public class SurveyService {

    private final SurveyRepository surveyRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    public List<Map<String, Object>> list() {
        return surveyRepository.findAll().stream().map(s -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", s.getId());
            item.put("title", s.getTitle());
            item.put("description", s.getDescription());
            item.put("questions", parseQuestions(s.getQuestions()));
            return item;
        }).toList();
    }

    /**
     * 提交答卷：按各题选项得分求和，匹配解读区间
     */
    public Map<String, Object> submit(Long surveyId, List<Integer> answers) {
        Survey survey = surveyRepository.findById(surveyId).orElseThrow(() -> new ApiException("问卷不存在"));
        List<Map<String, Object>> questions = parseQuestions(survey.getQuestions());
        if (answers.size() != questions.size()) {
            throw new ApiException("答卷题数与问卷不一致");
        }
        int total = 0;
        for (int i = 0; i < questions.size(); i++) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> options = (List<Map<String, Object>>) questions.get(i).get("options");
            int idx = answers.get(i);
            if (idx < 0 || idx >= options.size()) {
                throw new ApiException("第 " + (i + 1) + " 题答案不合法");
            }
            total += ((Number) options.get(idx).get("score")).intValue();
        }

        String level = "正常";
        String advice = "你的心理状态整体平稳，继续保持规律作息与运动习惯。";
        try {
            List<Map<String, Object>> ranges = mapper.readValue(survey.getInterpretation(),
                    new TypeReference<List<Map<String, Object>>>() {
                    });
            for (Map<String, Object> r : ranges) {
                int min = ((Number) r.get("min")).intValue();
                int max = ((Number) r.get("max")).intValue();
                if (total >= min && total <= max) {
                    level = String.valueOf(r.get("level"));
                    advice = String.valueOf(r.get("advice"));
                    break;
                }
            }
        } catch (Exception e) {
            throw new ApiException("问卷解读配置有误");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("score", total);
        result.put("level", level);
        result.put("advice", advice);
        result.put("title", survey.getTitle());
        return result;
    }

    private List<Map<String, Object>> parseQuestions(String json) {
        try {
            return mapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {
            });
        } catch (Exception e) {
            throw new ApiException("问卷题目解析失败");
        }
    }
}
