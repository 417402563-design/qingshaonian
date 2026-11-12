package cn.edu.sdua.qingshaonian.config;

import cn.edu.sdua.qingshaonian.entity.*;
import cn.edu.sdua.qingshaonian.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 首次启动时初始化种子数据（已存在则跳过，可安全重复运行）
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final KBCategoryRepository categoryRepository;
    private final KnowledgeItemRepository knowledgeRepository;
    private final RoleModelStoryRepository storyRepository;
    private final ExamContentRepository examRepository;
    private final ArticleRepository articleRepository;
    private final SurveyRepository surveyRepository;
    private final SystemConfigRepository configRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public DataInitializer(UserRepository userRepository, KBCategoryRepository categoryRepository,
                           KnowledgeItemRepository knowledgeRepository, RoleModelStoryRepository storyRepository,
                           ExamContentRepository examRepository, ArticleRepository articleRepository,
                           SurveyRepository surveyRepository, SystemConfigRepository configRepository) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.knowledgeRepository = knowledgeRepository;
        this.storyRepository = storyRepository;
        this.examRepository = examRepository;
        this.articleRepository = articleRepository;
        this.surveyRepository = surveyRepository;
        this.configRepository = configRepository;
    }

    @Override
    public void run(String... args) {
        initUsers();
        initCategories();
        initKnowledgeItems();
        initStories();
        initExamContents();
        initArticles();
        initSurveys();
        initConfigs();
    }

    private void initUsers() {
        if (userRepository.count() > 0) {
            return;
        }
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(encoder.encode("admin123"));
        admin.setRole(Role.ADMIN);
        admin.setNickname("系统管理员");
        admin.setStatus("ACTIVE");

        User counselor = new User();
        counselor.setUsername("counselor01");
        counselor.setPassword(encoder.encode("123456"));
        counselor.setRole(Role.COUNSELOR);
        counselor.setNickname("李老师");
        counselor.setTitle("国家二级心理咨询师");
        counselor.setSpecialty("考试焦虑,情绪管理,人际交往");
        counselor.setLicenseNo("PSY-2020-000123");
        counselor.setIntro("从事青少年心理咨询 8 年，陪伴超过 1200 名同学走过迷茫期。");
        counselor.setPermissions("case:view,case:edit,session:view");
        counselor.setStatus("ACTIVE");

        User student = new User();
        student.setUsername("student01");
        student.setPassword(encoder.encode("123456"));
        student.setRole(Role.STUDENT);
        student.setNickname("小雨");
        student.setSchool("成都市第七中学");
        student.setGrade("高二");
        student.setClassName("3班");
        student.setPsychProfileNo("PA2026000001");
        student.setStatus("ACTIVE");

        userRepository.saveAll(List.of(admin, counselor, student));
        log.info("已初始化示例账号：admin/admin123, counselor01/123456, student01/123456");
    }

    private void initCategories() {
        if (categoryRepository.count() > 0) {
            return;
        }
        categoryRepository.saveAll(List.of(
                category("学业压力", "作业多、跟不上、厌学、成绩波动等学习相关困扰", "压力,作业,厌学,跟不上,成绩,学习,努力,内耗,比较", 1),
                category("考试焦虑", "大考前紧张、失眠、大脑空白、担心考砸", "考试,高考,中考,紧张,失眠,考砸,复习,冲刺,大脑空白", 2),
                category("情绪管理", "情绪低落、烦躁、易怒、委屈等情绪调适", "情绪,低落,烦躁,愤怒,委屈,难过,崩溃,哭泣,心情", 3),
                category("人际交往", "与同学朋友的矛盾、被孤立、社交恐惧", "同学,朋友,孤立,排挤,吵架,社交,合群,室友,霸凌", 4),
                category("家庭关系", "与父母沟通困难、管教冲突、单亲留守", "爸妈,父母,家,代沟,批评,打骂,期待,单亲,留守", 5),
                category("青春期困惑", "自我认同、外貌焦虑、懵懂情感、未来迷茫", "青春,迷茫,外貌,喜欢,恋爱,自信,自尊,未来,目标", 6)));
        log.info("已初始化心理知识库常见问题分类");
    }

    private KBCategory category(String name, String desc, String keywords, int order) {
        KBCategory c = new KBCategory();
        c.setName(name);
        c.setDescription(desc);
        c.setKeywords(keywords);
        c.setSortOrder(order);
        return c;
    }

    private void initKnowledgeItems() {
        if (knowledgeRepository.count() > 0) {
            return;
        }
        knowledgeRepository.saveAll(List.of(
                item("考试前紧张失眠怎么办", "考试焦虑",
                        "考前焦虑非常常见，适度的紧张反而能帮助专注。可以尝试：1）睡前 4-7-8 呼吸法（吸气4秒、屏息7秒、呼气8秒）；2）把担心写在纸上再折起来，告诉大脑'已存档，明天再处理'；3）考前一晚不熬夜刷题，保证 6 小时以上睡眠比多刷 10 道题更有用。",
                        "失眠,紧张,呼吸,考前"),
                item("感觉怎么努力都提高不了成绩", "学业压力",
                        "付出看不到回报时会挫败，这很正常。先把'提高成绩'拆成小目标：比如本周只攻克一个薄弱题型；用错题归因代替自我批评（是概念不清、粗心还是时间分配问题）；每天记录一件'今天我做到了'的小事，让进步被看见。",
                        "努力,成绩,挫败,目标,错题"),
                item("一想到考试就大脑空白", "考试焦虑",
                        "大脑空白是应激反应，不是能力问题。平时模拟'考试场景脱敏'：限时做套卷训练手感；进考场前做三次深呼吸；考试中遇到空白，先跳过做会的题，让成功体验把大脑'重启'。",
                        "大脑空白,考试,呼吸,模拟"),
                item("情绪像坐过山车，控制不住发火", "情绪管理",
                        "青春期大脑中负责情绪的杏仁核发育快于负责理智的前额叶，情绪波动有生理原因。试试'情绪急救三步'：觉察（我在生气）、暂停（离开现场 60 秒）、表达（用'我感到…因为…我希望…'句式说出来而不是吼出来）。",
                        "情绪,发火,冲动,杏仁核,表达"),
                item("心情低落提不起劲，是不是抑郁了", "情绪管理",
                        "持续两周以上的情绪低落、兴趣减退、精力下降需要重视。可以先做到：保持规律作息与日照、每天 20 分钟运动、和信任的人说说话。如果低落感挥之不去，请告诉家长和老师，寻求专业心理老师帮助，这和感冒看医生一样平常。",
                        "低落,抑郁,兴趣,运动,求助"),
                item("感觉被同学孤立，没有朋友", "人际交往",
                        "被孤立的感受很疼。先分辨是'被排挤'还是'没找到同频的人'：前者不是你的错，应及时告诉老师家长；后者可以多从共同兴趣切入（社团、球赛、题目讨论），一次只交一个朋友，主动帮小忙比刻意讨好更有效。",
                        "孤立,朋友,排挤,社团,兴趣"),
                item("和父母说不到三句就吵架", "家庭关系",
                        "冲突常因为双方都在'证明自己对'。试试换时机（都平静时）、换句式（说感受不说评价：'我觉得压力很大'而非'你们只关心成绩'）、提具体请求（'你们能不能听我说完再给建议'）。父母也是第一次做父母，给他们学习的时间。",
                        "父母,吵架,沟通,代沟,批评"),
                item("爸妈总拿我和别人家孩子比较", "家庭关系",
                        "被比较时觉得自己'不够好'很伤人。要知道比较反映的是父母的焦虑，不是你的价值。可以认真告诉他们：'被比较让我更没动力，我需要你们看见我的努力'；同时建立自己的参照系——和上周的自己比，进步就值得肯定。",
                        "比较,父母,价值,努力"),
                item("对自己的长相很自卑", "青春期困惑",
                        "青春期对外貌敏感是大脑在发育的正常表现。接纳三件事：审美是多元且会变的；把注意力从'被看'转向'好用'（身体带你运动、弹琴、奔跑）；保持干净整齐、挺拔的体态比五官更能提升气质。若长期因此痛苦，可找心理老师聊聊。",
                        "外貌,自卑,长相,青春"),
                item("对未来很迷茫，不知道为了什么努力", "青春期困惑",
                        "迷茫说明你在思考，是成长的信号。不必急着找到'一生热爱'，用排除法认识自己：列出讨厌的事与喜欢的事；尝试最小成本体验（一门公开课、一次职业访谈）；把目标定成'变成更有力量的自己'，路会越走越清楚。",
                        "迷茫,未来,目标,兴趣,尝试")));
        log.info("已初始化心理知识库种子条目");
    }

    private KnowledgeItem item(String title, String category, String content, String tags) {
        KnowledgeItem i = new KnowledgeItem();
        i.setTitle(title);
        i.setCategoryCode(category);
        i.setContent(content);
        i.setTags(tags);
        i.setStatus("PUBLISHED");
        return i;
    }

    private void initStories() {
        if (storyRepository.count() > 0) {
            return;
        }
        storyRepository.saveAll(List.of(
                story("苏炳添：用科学和自律跑出 9 秒 83", "拼搏励志",
                        "亚洲短跑曾被认为存在天花板，32 岁的苏炳添用科研式训练重塑起跑技术，最终东京奥运跑出 9 秒 83。",
                        "苏炳添的成功不是天赋异禀，而是把每一个 0.01 秒拆解成可训练的动作：起跑步幅、摆臂角度、睡前恢复。他也曾在低谷被质疑'年纪大了该退役'，他的回应是录制训练视频逐帧分析。对同学们的启示：把'我不行'换成'我还没找到方法'，用记录和复盘代替自我怀疑，普通人的坚持同样能改写天花板。", true),
                story("全红婵：大跳台下的平常心", "心态调节",
                        "14 岁奥运夺冠、17 岁卫冕世锦赛，全红婵面对巨大关注说的是'我不是天才，我只是每次训练都全力以赴'。",
                        "少年成名背后是每天数百次的跳水练习，入水水花不到一手宽。更难得的是她面对失误的态度：跳坏了就笑一笑，上台前深呼吸，把注意力放在动作而不是分数上。大考之前，我们也可以学她——专注于'做好这一跳'，而不是'考砸了怎么办'。", true),
                story("樊锦诗：一辈子只做一件事", "理想坚守",
                        "北大毕业的樊锦诗扎根敦煌 60 年，从花季少女到'敦煌女儿'，用数字技术让莫高窟永存。",
                        "选择热门还是热爱？樊锦诗给出了跨越半个世纪的回答：把一件事做到极致，就是最酷的人生。迷茫时的同学不妨问自己：什么事情让我忘记时间？不必立刻有答案，但要像她守护洞窟一样，守护心里那粒种子。", false)));
        log.info("已初始化榜样故事");
    }

    private RoleModelStory story(String name, String tag, String summary, String content, boolean featured) {
        RoleModelStory s = new RoleModelStory();
        s.setName(name);
        s.setTag(tag);
        s.setSummary(summary);
        s.setContent(content);
        s.setFeatured(featured);
        s.setStatus("PUBLISHED");
        return s;
    }

    private void initExamContents() {
        if (examRepository.count() > 0) {
            return;
        }
        examRepository.saveAll(List.of(
                exam("考前一周心理冲刺指南", "冲刺心理",
                        "最后阶段，稳住心态就是提分。",
                        "1）停止刷难题新题，回归课本与错题本，巩固'确定性'；2）作息向考试时间表靠拢，让大脑在考试时段最兴奋；3）用'积极预演'代替灾难化想象：每天花 3 分钟想象自己从容答题的画面；4）接受波动：模考成绩起伏是正常现象，它只是体检报告，不是判决书。"),
                exam("考场突发慌乱的 60 秒急救法", "减压技巧",
                        "感到心跳加速、大脑空白时，先做这 60 秒。",
                        "第一步放下笔，双手握拳 5 秒再松开（重复两次）；第二步鼻吸 4 秒、嘴呼 6 秒，做 5 轮；第三步环顾考场确认'我在熟悉的环境'，最后暗示：'紧张说明我在乎，我准备好了'。绝大多数情况下，60 秒后你的专注就能回到试卷上。"),
                exam("备考期睡眠：被低估的提分利器", "备考策略",
                        "记忆在睡眠中巩固，熬夜刷题是最贵的'假努力'。",
                        "脑科学研究表明，白天输入的知识需要在深睡眠中整理归档。与其熬夜 1 小时多刷 10 题，不如保证 7 小时睡眠让这 10 题真正记住。考前两周开始固定 23:00 前入睡；睡不着也别焦虑，闭眼躺着同样是休息；午间小睡 20 分钟，下午效率翻倍。")));
        log.info("已初始化高考冲锋专栏");
    }

    private ExamContent exam(String title, String type, String summary, String content) {
        ExamContent e = new ExamContent();
        e.setTitle(title);
        e.setType(type);
        e.setSummary(summary);
        e.setContent(content);
        e.setStatus("PUBLISHED");
        return e;
    }

    private void initArticles() {
        if (articleRepository.count() > 0) {
            return;
        }
        articleRepository.saveAll(List.of(
                article("情绪的大脑说明书：为什么青春期的情绪这么强烈", "情绪",
                        "认识杏仁核与前额叶的时间差，你就不会责怪'容易激动的自己'了。",
                        "青春期大脑中负责情绪感知的杏仁核已高度活跃，而负责克制与规划的前额叶要到 25 岁左右才发育成熟。这就像一辆大马力汽车配了还在安装的刹车——情绪来得猛是生理使然，不是'不懂事'。好消息是：前额叶会在使用中成长，每一次'暂停 60 秒再回应'，都是在为它布线。"),
                article("被排挤不是你的错：识别与应对校园霸凌", "人际",
                        "沉默只会让霸凌升级，求助是强者的选择。",
                        "校园霸凌包括言语羞辱、社交孤立、财物勒索与网络攻击。如果你正在经历：记住三点——1）错的是施暴者，不是你；2）保留证据（截图、伤情、见证同学）；3）第一时间告诉家长、老师或拨打 12355 青少年服务热线。旁观者的一声'别这样'，往往就是转机的开始。"),
                article("和父母的'权力争夺'：如何吵架不伤感情", "家庭",
                        "把'你 VS 爸妈'换成'你们俩 VS 问题'。",
                        "亲子冲突多围绕手机、成绩、交友。心理学中的'非暴力沟通'四步很适合家庭场景：观察（这周我们因为手机吵了三次）、感受（我觉得很挫败）、需要（我需要一个被信任的空间）、请求（我们能不能一起定一个都认可的规则）。记住：目标不是赢，而是被理解。"),
                article("手机之外的世界：重建专注力的 21 天", "成长",
                        "不是要你戒断，而是把主动权拿回来。",
                        "短视频用 15 秒刺激喂饱你的多巴胺系统，长此以往课本的'慢反馈'就显得无聊。重建专注力可以用 21 天计划：第 1 周记录每天屏幕时长（觉察即改变）；第 2 周设定'无手机时段'（从 30 分钟自习开始）；第 3 周用运动、乐器、纸质书填补空出来的时间。坚持三周，你会重新发现'深度投入'的快乐。")));
        log.info("已初始化成长驿站科普文章");
    }

    private Article article(String title, String category, String summary, String content) {
        Article a = new Article();
        a.setTitle(title);
        a.setCategory(category);
        a.setSummary(summary);
        a.setContent(content);
        a.setStatus("PUBLISHED");
        return a;
    }

    private void initSurveys() {
        if (surveyRepository.count() > 0) {
            return;
        }
        Survey anxiety = new Survey();
        anxiety.setTitle("考试焦虑自评简表");
        anxiety.setDescription("8 题快速了解你的考前焦虑水平，选择最符合你最近两周状态的选项。");
        anxiety.setQuestions("""
                [{"title":"一想到即将到来的考试，我就感到紧张","options":[{"label":"从不","score":0},{"label":"偶尔","score":1},{"label":"经常","score":2},{"label":"总是","score":3}]},
                 {"title":"考试前出现失眠或入睡困难","options":[{"label":"从不","score":0},{"label":"偶尔","score":1},{"label":"经常","score":2},{"label":"总是","score":3}]},
                 {"title":"答题时出现过大脑一片空白的情况","options":[{"label":"从不","score":0},{"label":"偶尔","score":1},{"label":"经常","score":2},{"label":"总是","score":3}]},
                 {"title":"担心考不好会让父母和老师失望","options":[{"label":"从不","score":0},{"label":"偶尔","score":1},{"label":"经常","score":2},{"label":"总是","score":3}]},
                 {"title":"考试临近时容易因小事发脾气","options":[{"label":"从不","score":0},{"label":"偶尔","score":1},{"label":"经常","score":2},{"label":"总是","score":3}]},
                 {"title":"会出现心慌、手心出汗等身体反应","options":[{"label":"从不","score":0},{"label":"偶尔","score":1},{"label":"经常","score":2},{"label":"总是","score":3}]},
                 {"title":"总觉得复习时间不够用，越复习越慌","options":[{"label":"从不","score":0},{"label":"偶尔","score":1},{"label":"经常","score":2},{"label":"总是","score":3}]},
                 {"title":"考后长时间反复回想错题，难以放下","options":[{"label":"从不","score":0},{"label":"偶尔","score":1},{"label":"经常","score":2},{"label":"总是","score":3}]}]
                """);
        anxiety.setInterpretation("""
                [{"min":0,"max":6,"level":"焦虑较低","advice":"你的考前状态平稳，保持现有节奏即可，注意劳逸结合。"},
                 {"min":7,"max":13,"level":"中等焦虑","advice":"适度焦虑有助于发挥。建议每天练习 4-7-8 呼吸法，考前一周调整作息；如果某道题反复困扰你，说明压力集中在具体环节，可以找老师定点突破。"},
                 {"min":14,"max":24,"level":"焦虑偏高","advice":"焦虑已影响睡眠或身体反应，建议和家长、心理老师说说，使用'担忧时间'技术（每天固定 15 分钟集中担忧，其余时间写下来存档）。必要时寻求专业心理辅导，这不是软弱，是自我照顾。"}]
                """);

        Survey mood = new Survey();
        mood.setTitle("一周情绪自查量表");
        mood.setDescription("6 题回顾最近一周的整体情绪状态，帮助你看见自己。");
        mood.setQuestions("""
                [{"title":"这周大部分时间感到心情不错","options":[{"label":"总是","score":2},{"label":"经常","score":1},{"label":"很少","score":0},{"label":"从不","score":0}]},
                 {"title":"这周对喜欢的事情（运动、爱好）仍有兴趣","options":[{"label":"总是","score":2},{"label":"经常","score":1},{"label":"很少","score":0},{"label":"从不","score":0}]},
                 {"title":"这周能顺利集中注意力完成学习任务","options":[{"label":"总是","score":2},{"label":"经常","score":1},{"label":"很少","score":0},{"label":"从不","score":0}]},
                 {"title":"这周与家人朋友的相处让我感到支持","options":[{"label":"总是","score":2},{"label":"经常","score":1},{"label":"很少","score":0},{"label":"从不","score":0}]},
                 {"title":"这周没有频繁的委屈、想哭或烦躁时刻","options":[{"label":"总是","score":2},{"label":"经常","score":1},{"label":"很少","score":0},{"label":"从不","score":0}]},
                 {"title":"这周对自己的评价是积极的","options":[{"label":"总是","score":2},{"label":"经常","score":1},{"label":"很少","score":0},{"label":"从不","score":0}]}]
                """);
        mood.setInterpretation("""
                [{"min":9,"max":12,"level":"情绪阳光","advice":"你的心理能量充足，记得把这份状态分享给身边的人，也保持规律作息让好状态可持续。"},
                 {"min":5,"max":8,"level":"情绪多云","advice":"有些疲惫和波动是正常的。试着每天留 30 分钟给自己喜欢的事，和信任的人聊聊天；写下三件小确幸，帮助大脑重新注意到光亮。"},
                 {"min":0,"max":4,"level":"情绪有雨","advice":"这周你辛苦了。持续的低落值得被认真对待，请告诉家长或学校心理老师你的感受；拨打 12355 也可以获得随时在线的倾听陪伴。你不是一个人在撑。"}]
                """);
        surveyRepository.saveAll(List.of(anxiety, mood));
        log.info("已初始化自我探索测评问卷");
    }

    private void initConfigs() {
        if (configRepository.count() > 0) {
            return;
        }
        configRepository.saveAll(List.of(
                config("platform.name", "青少年心理辅导咨询系统", "平台显示名称"),
                config("crisis.hotline", "12355,12356", "页面展示的危机干预热线"),
                config("consult.welcome", "你好呀，我是心理小屋的倾听伙伴，这里没有评判，只有陪伴。今天想聊点什么？", "智能咨询欢迎语"),
                config("consult.open-hours", "全天开放", "智能咨询开放时间")));
        log.info("已初始化系统配置项");
    }

    private SystemConfig config(String key, String value, String desc) {
        SystemConfig c = new SystemConfig();
        c.setConfigKey(key);
        c.setConfigValue(value);
        c.setDescription(desc);
        return c;
    }
}
