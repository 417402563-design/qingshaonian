package cn.edu.sdua.qingshaonian.service;

import cn.edu.sdua.qingshaonian.dto.ApiException;
import cn.edu.sdua.qingshaonian.dto.Dtos;
import cn.edu.sdua.qingshaonian.entity.ChatMessage;
import cn.edu.sdua.qingshaonian.entity.ChatSession;
import cn.edu.sdua.qingshaonian.entity.InterventionCase;
import cn.edu.sdua.qingshaonian.repository.ChatMessageRepository;
import cn.edu.sdua.qingshaonian.repository.ChatSessionRepository;
import cn.edu.sdua.qingshaonian.repository.InterventionCaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 智能咨询核心服务：构建"学生提问 - 双路径应答 - 咨询师介入"流程
 * 路径一：知识库检索增强 + DeepSeek 共情安抚回答
 * 路径二：危机词命中 -> 风险标记 -> 自动创建干预案例转人工
 */
@Service
@RequiredArgsConstructor
public class ChatService {

    private static final String SYSTEM_PROMPT_TEMPLATE = """
            你是"青少年心理小屋"的 AI 心理倾听伙伴，面向中国青少年学生。请遵循以下原则：
            1. 以倾听、共情、温暖、不评判的态度回应，先接住情绪，再讨论问题；
            2. 回答控制在 300 字以内，口语化、亲切，可以使用轻量 emoji，禁止说教；
            3. 结合下面的心理知识库内容给出科学建议（如认知重评、正念呼吸、时间管理等具体方法）；
            4. 你不是治疗师，不做诊断；如果用户流露出伤害自己的念头，务必温和地引导其联系老师、家长，并告知 12355 青少年服务热线与全国 24 小时心理援助热线 12356；
            5. 只输出回复正文，不要输出分析过程。
            【心理知识库参考内容】
            %s
            """;

    private static final String CRISIS_REPLY = """
            我听到了你现在的痛苦，你愿意说出来，这非常勇敢，我在陪着你。🌙
            这样吧，我们先做一件小事：深呼吸三次，然后联系一位你现在最信任的大人——爸爸妈妈、老师或者好朋友，让他们陪在你身边。
            你也可以随时拨打 12355 青少年服务热线，或全国 24 小时心理援助热线 12356，那里会有专业的人立即帮助你。
            我已经通知了平台的心理老师，她会尽快与你联系。请给自己一个被帮助的机会，好吗？
            """;

    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;
    private final InterventionCaseRepository caseRepository;
    private final KnowledgeRetrievalService retrievalService;
    private final RiskDetector riskDetector;
    private final DeepSeekService deepSeekService;

    @Transactional
    public Dtos.ChatReply ask(Long userId, Dtos.ChatRequest req) {
        ChatSession session = resolveSession(userId, req.sessionId(), req.content());
        String question = req.content().trim();

        ChatMessage userMsg = new ChatMessage();
        userMsg.setSessionId(session.getId());
        userMsg.setRole("user");
        userMsg.setContent(question);
        messageRepository.save(userMsg);

        RiskDetector.Detection detection = riskDetector.detect(question);
        session.setEmotionTag(detection.emotionTag());

        // 路径二：危机命中 -> 安抚话术 + 标记风险 + 自动转人工
        if ("HIGH".equals(detection.riskLevel())) {
            session.setRiskLevel("HIGH");
            String reply = CRISIS_REPLY;
            KnowledgeRetrievalService.Retrieval retrieval = retrievalService.retrieve(question);
            DeepSeekService.ChatResult modelResult = callModel(session.getId(), SYSTEM_PROMPT_TEMPLATE.formatted(retrieval.context()));
            if (modelResult != null) {
                reply = modelResult.content() + "\n\n【危机支持】请尽快联系身边信任的大人，或拨打 12355 / 12356 心理援助热线，平台心理老师也会尽快联系你。";
            }
            ChatMessage assistantMsg = saveAssistant(session, userMsg, reply, retrieval.categoryCode(),
                    detection, modelResult != null ? "DeepSeek" : "CRISIS_PROTOCOL",
                    modelResult == null ? null : modelResult.tokens(),
                    modelResult == null ? null : modelResult.latencyMs());
            escalateToCounselor(session, question);
            sessionRepository.save(session);
            return new Dtos.ChatReply(session.getId(), assistantMsg.getId(), reply, retrieval.categoryCode(),
                    detection.emotionTag(), "HIGH", assistantMsg.getSource(), false,
                    List.of("帮我联系心理老师", "我想了解热线怎么打", "我现在好多了"));
        }

        // 路径一：知识库检索增强 + 大模型共情回答
        KnowledgeRetrievalService.Retrieval retrieval = retrievalService.retrieve(question);
        if (retrieval.categoryCode() != null) {
            session.setCategoryCode(retrieval.categoryCode());
        }
        if ("MEDIUM".equals(detection.riskLevel()) && "NONE".equals(session.getRiskLevel())) {
            session.setRiskLevel("MEDIUM");
        }

        String reply;
        String source;
        Integer tokens = null;
        Integer latency = null;
        boolean isFallback;
        DeepSeekService.ChatResult modelResult = callModel(session.getId(), SYSTEM_PROMPT_TEMPLATE.formatted(retrieval.context()));
        if (modelResult != null) {
            reply = modelResult.content();
            source = "DeepSeek";
            tokens = modelResult.tokens();
            latency = modelResult.latencyMs();
            isFallback = false;
        } else {
            reply = templateReply(question, retrieval);
            source = "KNOWLEDGE_BASE";
            isFallback = true;
        }
        ChatMessage assistantMsg = saveAssistant(session, userMsg, reply, retrieval.categoryCode(), detection, source, tokens, latency);
        session.setUpdatedAt(LocalDateTime.now());
        sessionRepository.save(session);

        List<String> suggestions = retrieval.categories().isEmpty()
                ? List.of("最近学习压力好大", "和同学相处不舒服", "心情一直很低落")
                : retrieval.categories().stream().limit(3).map(c -> "聊聊" + c.getName()).toList();
        return new Dtos.ChatReply(session.getId(), assistantMsg.getId(), reply, retrieval.categoryCode(),
                detection.emotionTag(), session.getRiskLevel(), source, isFallback, suggestions);
    }

    private DeepSeekService.ChatResult callModel(Long sessionId, String systemPrompt) {
        if (!deepSeekService.available()) {
            return null;
        }
        List<ChatMessage> history = messageRepository.findBySessionIdOrderByIdAsc(sessionId);
        List<Map<String, String>> messages = new ArrayList<>();
        history.stream().skip(Math.max(0, history.size() - 10)).forEach(m ->
                messages.add(Map.of("role", m.getRole(), "content", m.getContent())));
        return deepSeekService.chat(systemPrompt, messages);
    }

    /** 未配置大模型时的知识库模板回答（保证心理安抚逻辑可用） */
    private String templateReply(String question, KnowledgeRetrievalService.Retrieval retrieval) {
        StringBuilder sb = new StringBuilder();
        sb.append("我感受到你现在不太舒服，愿意和我说说这些，本身就是在照顾自己。先给你一个隔空的拥抱。🌱\n");
        if (!retrieval.items().isEmpty()) {
            var item = retrieval.items().get(0);
            sb.append("关于你提到的「").append(retrieval.categoryCode() == null ? item.getTitle() : retrieval.categoryCode())
                    .append("」，这里有一些也许有帮助的思路：\n")
                    .append(item.getContent()).append("\n");
            if (retrieval.items().size() > 1) {
                sb.append("如果还想继续了解，可以问我：").append(retrieval.items().get(1).getTitle()).append("。");
            }
        } else {
            sb.append("可以先试着把情绪写下来，做几次深呼吸（吸气 4 秒、屏息 2 秒、呼气 6 秒），等平静一些后我们再一起梳理。").append(question).append(" 背后也许藏着未被看见的压力。如果你愿意，多说一点细节，我会一直在这里听你说。");
        }
        return sb.toString();
    }

    private ChatSession resolveSession(Long userId, Long sessionId, String content) {
        ChatSession session;
        if (sessionId == null) {
            session = new ChatSession();
            session.setUserId(userId);
            String title = content.trim();
            session.setTitle(title.length() > 20 ? title.substring(0, 20) : title);
            sessionRepository.save(session);
        } else {
            session = sessionRepository.findById(sessionId)
                    .orElseThrow(() -> new ApiException("会话不存在"));
            if (!session.getUserId().equals(userId)) {
                throw new ApiException(403, "无权访问该会话");
            }
        }
        return session;
    }

    private ChatMessage saveAssistant(ChatSession session, ChatMessage userMsg, String reply, String categoryCode,
                                      RiskDetector.Detection detection, String source, Integer tokens, Integer latency) {
        userMsg.setCategoryCode(categoryCode);
        userMsg.setEmotionTag(detection.emotionTag());
        userMsg.setRiskFlag(!"NONE".equals(detection.riskLevel()));
        messageRepository.save(userMsg);

        ChatMessage msg = new ChatMessage();
        msg.setSessionId(session.getId());
        msg.setRole("assistant");
        msg.setContent(reply);
        msg.setCategoryCode(categoryCode);
        msg.setEmotionTag(detection.emotionTag());
        msg.setRiskFlag(!"NONE".equals(detection.riskLevel()));
        msg.setSource(source);
        msg.setTokens(tokens);
        msg.setLatencyMs(latency);
        return messageRepository.save(msg);
    }

    /** 危机会话自动生成干预案例，进入咨询师工作台 */
    private void escalateToCounselor(ChatSession session, String question) {
        if (caseRepository.findBySourceSessionId(session.getId()).isPresent()) {
            return;
        }
        InterventionCase caseEntity = new InterventionCase();
        caseEntity.setStudentId(session.getUserId());
        caseEntity.setLevel("危机");
        caseEntity.setProblemCategory(session.getCategoryCode());
        caseEntity.setSummary("智能咨询检测到危机关键词，自动生成。学生表述：" +
                (question.length() > 100 ? question.substring(0, 100) : question));
        caseEntity.setSourceSessionId(session.getId());
        caseEntity.setStatus("OPEN");
        caseRepository.save(caseEntity);
        session.setStatus("ESCALATED");
    }

    @Transactional
    public Long escalate(Long userId, Long sessionId) {
        ChatSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ApiException("会话不存在"));
        if (!session.getUserId().equals(userId)) {
            throw new ApiException(403, "无权操作该会话");
        }
        if ("NONE".equals(session.getRiskLevel())) {
            session.setRiskLevel("MEDIUM");
        }
        escalateToCounselor(session, lastQuestion(sessionId));
        sessionRepository.save(session);
        return session.getId();
    }

    private String lastQuestion(Long sessionId) {
        List<ChatMessage> list = messageRepository.findByRoleAndSessionIdOrderByIdAsc("user", sessionId);
        return list.isEmpty() ? "" : list.get(list.size() - 1).getContent();
    }

    public List<ChatSession> mySessions(Long userId) {
        return sessionRepository.findByUserIdOrderByUpdatedAtDesc(userId);
    }

    public List<ChatMessage> messages(Long userId, Long sessionId) {
        ChatSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ApiException("会话不存在"));
        if (!session.getUserId().equals(userId)) {
            throw new ApiException(403, "无权访问该会话");
        }
        return messageRepository.findBySessionIdOrderByIdAsc(sessionId);
    }
}
