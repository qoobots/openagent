package com.qoobot.agent.education_agent.service.impl;

import com.qoobot.agent.education_agent.service.ContentSafetyService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

/**
 * 内容安全审核实现
 *
 * <p>采用基于关键词 + 正则 + AC 自动机思想的轻量实现。
 * 敏感词库优先从 classpath:sensitive-words.txt 加载，加载失败时使用内置默认词库。
 */
@Slf4j
@Service
public class ContentSafetyServiceImpl implements ContentSafetyService {

    /** 默认敏感词库（当外部文件缺失时兜底） */
    private static final List<String> DEFAULT_SENSITIVE_WORDS = List.of(
            // 政治敏感
            "反动", "法轮", "台独", "港独", "疆独", "藏独",
            // 暴力恐怖
            "恐怖袭击", "制造炸弹", "爆炸物制作", "枪支贩卖",
            // 色情低俗
            "色情", "裸聊", "约炮", "一夜情", "卖淫", "嫖娼",
            // 毒品
            "冰毒", "海洛因", "大麻", "K 粉", "摇头丸", "制毒",
            // 赌博
            "网络赌博", "澳门威尼斯", "博彩网站", "老虎机", "百家乐赌博",
            // 校园霸凌
            "打死你", "弄死你", "自杀方法", "如何自残",
            // 未成年人不当
            "未成年人怀孕", "少女援交"
    );

    /** 敏感词正则字符转义 */
    private static final Pattern SPECIAL_CHARS = Pattern.compile("[.*+?^${}()|\\[\\]\\\\]");

    private final AtomicReference<List<String>> sensitiveWords = new AtomicReference<>(Collections.emptyList());

    @PostConstruct
    public void init() {
        List<String> words = new ArrayList<>(DEFAULT_SENSITIVE_WORDS);
        try {
            ClassPathResource resource = new ClassPathResource("sensitive-words.txt");
            if (resource.exists()) {
                String content = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
                String[] lines = content.split("\\r?\\n");
                for (String line : lines) {
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                        words.add(trimmed);
                    }
                }
                log.info("从 sensitive-words.txt 加载敏感词");
            }
        } catch (IOException e) {
            log.warn("未找到 sensitive-words.txt，使用默认敏感词库: {}", e.getMessage());
        }
        // 去重 + 排序
        words = words.stream().distinct().sorted((a, b) -> Integer.compare(b.length(), a.length())).toList();
        sensitiveWords.set(words);
        log.info("敏感词库初始化完成，词条数: {}", words.size());
    }

    @Override
    public boolean check(String text) {
        return checkAndExplain(text) == null;
    }

    @Override
    public String checkAndExplain(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String lower = text.toLowerCase();
        for (String word : sensitiveWords.get()) {
            if (lower.contains(word.toLowerCase())) {
                return "检测到敏感词: " + word;
            }
        }
        return null;
    }

    @Override
    public String mask(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        String result = text;
        for (String word : sensitiveWords.get()) {
            result = result.replaceAll("(?i)" + escapeRegex(word), "*".repeat(word.length()));
        }
        return result;
    }

    private String escapeRegex(String word) {
        return SPECIAL_CHARS.matcher(word).replaceAll("\\\\$0");
    }
}
