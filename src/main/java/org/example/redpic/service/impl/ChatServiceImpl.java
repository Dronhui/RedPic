package org.example.redpic.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.redpic.entity.ChatHistory;
import org.example.redpic.exception.BizException;
import org.example.redpic.mapper.ChatHistoryMapper;
import org.example.redpic.service.ChatService;
import org.example.redpic.service.RagSearchService;
import org.example.redpic.service.VisionService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ChatServiceImpl implements ChatService {

        private final ChatClient memoryChatClient;
        private final RagSearchService ragSearchService;
        private final ChatClient rewriteChatClient;
        private final VisionService visionService;
        private final MessageWindowChatMemory aiChatMemory;
        private final Map<String,String> chatPersonMap = new HashMap<>();
        private final Map<String,String> ragPersonMap = new HashMap<>();
        private final String rewritePrompt;
        private final ChatHistoryMapper chatHistoryMapper;

        public ChatServiceImpl(VisionService visionService, RagSearchService ragSearchService
                , @Qualifier("memoryChatClient") ChatClient memoryChatClient, ChatHistoryMapper chatHistoryMapper
                , @Qualifier("rewriteChatClient") ChatClient rewriteChatClient
                , @Qualifier("aiChatMemory") MessageWindowChatMemory aiChatMemory
        ) {
            this.aiChatMemory = aiChatMemory;
            this.memoryChatClient = memoryChatClient;
            this.ragSearchService = ragSearchService;
            this.visionService = visionService;
            this.chatHistoryMapper = chatHistoryMapper;
            this.rewriteChatClient = rewriteChatClient;

            try {
                chatPersonMap.put("default",new String(new ClassPathResource("prompts/default.st").getInputStream().readAllBytes(),"UTF-8"));
                chatPersonMap.put("expert",new String(new ClassPathResource("prompts/expert.st").getInputStream().readAllBytes(),"UTF-8"));
                ragPersonMap.put("default",new String(new ClassPathResource("prompts/rag_default.st").getInputStream().readAllBytes(),"UTF-8"));
                ragPersonMap.put("expert",new String(new ClassPathResource("prompts/rag_expert.st").getInputStream().readAllBytes(),"UTF-8"));
                rewritePrompt = new String(new ClassPathResource("prompts/rewrite.st").getInputStream().readAllBytes(),"UTF-8");
            } catch (IOException e) {
                throw new RuntimeException("读取提示词失败",e);
            }



        }
    private String readPrompt(String path) throws IOException {
        return new String(new ClassPathResource(path).getInputStream().readAllBytes(), "UTF-8");
    }

        @Override
        public Flux<String> chat(MultipartFile image, String question, boolean useRag, String type, String sessionId) {
            String finalSessionId = (sessionId==null||sessionId.isBlank())? UUID.randomUUID().toString():sessionId;
            String currentUserId = cn.dev33.satoken.stp.StpUtil.getLoginIdAsString();
            String rawQuestion = (question == null) ? "" : question.trim();
            String imageEntity = null;
            if(image!=null&&!image.isEmpty()){
                imageEntity = visionService.analyzeImage(image,"请识别主体");
                System.out.println("用户上传了图片，图片中核心主体是："+imageEntity);
                if (imageEntity.contains("未知") || imageEntity.contains("未识别") || imageEntity.contains("失败")) {
                    // 直接抛出业务异常，让前端弹红字提示，不走后续的大模型和检索逻辑
                    throw new BizException("同志，我未能看清图片中的核心主体，请换一张清晰的照片，或直接输入名称告诉我哦。");
                }
            }
            final String finalQuestion ;
            if(rawQuestion.isEmpty()&&imageEntity!=null){
                finalQuestion = "请介绍一下图片中主体的相关信息"+imageEntity;
            }
            else{
                finalQuestion = rawQuestion;
            }
             System.out.println("用户问题："+finalQuestion);
            String finalQuestionRewrite ;
            if(imageEntity==null){
                finalQuestionRewrite = finalQuestion;
            }
            else{
                finalQuestionRewrite = "【提示：用户当前上传的图片主体是：" + imageEntity + "】 用户原问题：" + finalQuestion;
            }
             System.out.println("用户问题重写后："+finalQuestionRewrite);

            String finalSystemPrompt;
            if(useRag){
                String historyText = buildRecentHistory(finalSessionId, 3);
                String rewrite = rewritePrompt
                        .replace("{history}",historyText)
                        .replace("{question}",finalQuestionRewrite);
                String rewriteQuestion = rewriteChatClient.prompt().user(rewrite)
                         .call().content();
                RewriteResult result = parseRewriteResult(rewriteQuestion, finalQuestionRewrite);
                System.out.println("用户问题重写后：" + result.query + " | 意图：" + result.intentList
                        + " | 解析层级：" + result.parseLevel + " | 触发降级：" + result.fallback);

                String docs = ragSearchService.searchDocuments(result.query, result.intentList, result.fallback);
                if(docs==null||docs.isEmpty()){
                    docs = "此问题本地知识库未收入";
                }
                String pathPrompt = ragPersonMap.getOrDefault(type, ragPersonMap.get("default"));
                finalSystemPrompt = pathPrompt.replace("{docs}",docs);

            }
            else{
                finalSystemPrompt = chatPersonMap.getOrDefault(type, chatPersonMap.get("default"));

            }

            StringBuilder fullAiResponse = new StringBuilder();

             return memoryChatClient.prompt().system(finalSystemPrompt).
                    user(finalQuestion).advisors(a->a.param(ChatMemory.CONVERSATION_ID,finalSessionId))
                    .stream().content().map(a->{
                        fullAiResponse.append(a);
                        return a.replace("\r","")
                                .replace("\t","");
                    }).doOnComplete(()->{
                         try {
                             String finalAnswer = fullAiResponse.toString().trim();
                             ChatHistory chatHistory = new ChatHistory();
                             chatHistory.setUserId(currentUserId);
                             chatHistory.setSessionId(finalSessionId);
                             chatHistory.setUserMessage(finalQuestion);
                             chatHistory.setAiMessage(finalAnswer);
                             chatHistory.setCreateTime(LocalDateTime.now());
                             chatHistoryMapper.insert(chatHistory);
                             System.out.println("聊天记录成功存入数据库");
                         } catch (Exception e) {
                             System.err.println("聊天记录存入数据库失败："+e.getMessage());
                         }
                     });

        }


        private String buildRecentHistory(String sessionId, int rounds) {
            List<Message> history = aiChatMemory.get(sessionId);   // get 只读，不触发写回
            if (history == null || history.isEmpty()) {
            return "（无）";
         }
            int from = Math.max(0, history.size() - rounds * 2);
            return history.subList(from, history.size()).stream()
                .map(m -> {
                    String role = (m.getMessageType() == MessageType.USER) ? "用户" : "助手";
                    return role + "：" + m.getText();
                })
                .collect(Collectors.joining("\n"));
        }


    public static class RewriteResult {
        public String query;
        public List<String> intentList;
        public boolean fallback; // true 表示完全降级
        public int parseLevel;   // 1:标准解析, 2:正则抢救, 3:完全降级

        public RewriteResult(String query, List<String> intentList, boolean fallback, int parseLevel) {
            this.query = query;
            this.intentList = intentList;
            this.fallback = fallback;
            this.parseLevel = parseLevel;
        }
    }

    /**
     * ★ 核心方法：三级解析状态机
     */
    private RewriteResult parseRewriteResult(String rawResult, String originalQuestion) {
        List<String> validTags = List.of("heroes", "sites", "stories", "all");
        String cleanResult = rawResult.replace("```json", "").replace("```", "").trim();

        // ---------- 一级：标准 JSON 解析 + 严格字段校验 ----------
        try {
            int start = cleanResult.indexOf("{");
            int end = cleanResult.lastIndexOf("}");
            if (start != -1 && end != -1) {
                String jsonStr = cleanResult.substring(start, end + 1);
                JsonNode node = new ObjectMapper().readTree(jsonStr);

                String query = node.has("query") ? node.get("query").asText().trim() : "";
                List<String> intents = new ArrayList<>();
                boolean hasIllegalTag = false;

                if (node.has("intent") && node.get("intent").isArray()) {
                    for (JsonNode n : node.get("intent")) {
                        String tag = n.asText().trim();
                        if (!validTags.contains(tag)) hasIllegalTag = true;
                        if (!intents.contains(tag)) intents.add(tag);
                    }
                }

                // 严格校验：Query非空 && Intent非空 && 无非法标签 && all不能与其他混用
                if (!query.isEmpty() && !intents.isEmpty() && !hasIllegalTag
                        && !(intents.contains("all") && intents.size() > 1)) {
                    return new RewriteResult(query, intents, false, 1);
                }
            }
        } catch (Exception ignored) { }

        // ---------- 二级：正则抢救解析 + 严格校验 ----------
        try {
            java.util.regex.Pattern qPattern = java.util.regex.Pattern.compile("\"query\"\\s*:\\s*\"([^\"]+)\"");
            java.util.regex.Matcher qMatcher = qPattern.matcher(cleanResult);

            java.util.regex.Pattern iPattern = java.util.regex.Pattern.compile("\"intent\"\\s*:\\s*\\[([^\\]]+)\\]");
            java.util.regex.Matcher iMatcher = iPattern.matcher(cleanResult);

            if (qMatcher.find() && iMatcher.find()) {
                String q = qMatcher.group(1).trim();
                String iStr = iMatcher.group(1);

                List<String> intents = new ArrayList<>();
                for (String tag : iStr.split(",")) {
                    tag = tag.replace("\"", "").trim();
                    if (!tag.isEmpty() && validTags.contains(tag) && !intents.contains(tag)) {
                        intents.add(tag);
                    }
                }

                if (!q.isEmpty() && !intents.isEmpty()
                        && !(intents.contains("all") && intents.size() > 1)) {
                    return new RewriteResult(q, intents, false, 2);
                }
            }
        } catch (Exception ignored) { }

        // ---------- 三级：完全降级 (Fallback) ----------
        // 解析彻底失败，使用原问题，意图 all，标记 fallback = true
        return new RewriteResult(originalQuestion, List.of("all"), true, 3);
    }

    @Override
    public List<Map<String, String>> getSessionHistory(String sessionId) {
            String userId = cn.dev33.satoken.stp.StpUtil.getLoginIdAsString();
            List<ChatHistory> chatHistories = chatHistoryMapper.selectList(new QueryWrapper<ChatHistory>()
                    .eq("user_id",userId)
                    .eq("session_id",sessionId)
                    .orderByAsc("create_time")
        );
           List<Map<String, String>> historyList = new ArrayList<>();
           for(ChatHistory chatHistory:chatHistories){
               if(chatHistory.getUserMessage()!=null&&!chatHistory.getUserMessage().isEmpty()){
                  Map<String, String> userMsg = new HashMap<>();
                  userMsg.put("role","user");
                  userMsg.put("content",chatHistory.getUserMessage());
                  historyList.add(userMsg);
               }
               if(chatHistory.getAiMessage()!=null&&!chatHistory.getAiMessage().isEmpty()){
                  Map<String, String> aiMsg = new HashMap<>();
                  aiMsg.put("role","assistant");
                  aiMsg.put("content",chatHistory.getAiMessage());
                  historyList.add(aiMsg);
               }
           }
           return historyList;


    }

    @Override
    public List<Map<String, Object>> getUserSessions() {
        String userId = cn.dev33.satoken.stp.StpUtil.getLoginIdAsString();
        List<ChatHistory> chatHistoriesList = chatHistoryMapper.selectList(new QueryWrapper<ChatHistory>().eq("user_id", userId)
                .orderByAsc("create_time")
        );
        Map<String, Map<String, Object>> sessionMap = new LinkedHashMap<>();
        for (ChatHistory chatHistory : chatHistoriesList) {
            if (!sessionMap.containsKey(chatHistory.getSessionId())) {
                Map<String, Object> sessioninfo = new HashMap<>();
                sessioninfo.put("sessionId", chatHistory.getSessionId());
                String title = chatHistory.getUserMessage();
                sessioninfo.put("title", title.length() >= 15 ? title.substring(0, 15) + "..." : title);
                sessioninfo.put("lastActiveTime", chatHistory.getCreateTime());
                sessionMap.put(chatHistory.getSessionId(), sessioninfo);

            }else {
                Map<String, Object> sessioninfo = sessionMap.get(chatHistory.getSessionId());
                sessioninfo.put("lastActiveTime", chatHistory.getCreateTime());
            }


        }
        List<Map<String, Object>> resultList = new ArrayList<>(sessionMap.values());
        resultList.sort((a, b) -> {
            LocalDateTime timeA = (LocalDateTime) a.get("lastActiveTime");
            LocalDateTime timeB = (LocalDateTime) b.get("lastActiveTime");
            return timeB.compareTo(timeA);
        });
        return  resultList;

    }
}
