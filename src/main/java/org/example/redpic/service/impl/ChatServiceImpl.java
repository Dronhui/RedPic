package org.example.redpic.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.redpic.entity.ChatHistory;
import org.example.redpic.mapper.ChatHistoryMapper;
import org.example.redpic.service.ChatService;
import org.example.redpic.service.RagSearchService;
import org.example.redpic.service.VisionService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ChatServiceImpl implements ChatService {

        private final ChatClient memoryChatClient;
        private final RagSearchService ragSearchService;
        private final VisionService visionService;
        private final Map<String,String> chatPersonMap = new HashMap<>();
        private final Map<String,String> ragPersonMap = new HashMap<>();
        private final String rewritePrompt;
        private final ChatHistoryMapper chatHistoryMapper;

        public ChatServiceImpl(VisionService visionService, RagSearchService ragSearchService
                , @Qualifier("memoryChatClient") ChatClient memoryChatClient, ChatHistoryMapper chatHistoryMapper) {
            this.memoryChatClient = memoryChatClient;
            this.ragSearchService = ragSearchService;
            this.visionService = visionService;
            this.chatHistoryMapper = chatHistoryMapper;
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

        @Override
        public Flux<String> chat(MultipartFile image, String question, boolean useRag, String type, String sessionId) {
            String finalSessionId = (sessionId==null||sessionId.isBlank())? UUID.randomUUID().toString():sessionId;
            String currentUserId = cn.dev33.satoken.stp.StpUtil.getLoginIdAsString();
            if(image!=null&&!image.isEmpty()){
                String entity = visionService.analyzeImage(image,"请识别主体");
                memoryChatClient.prompt().user("用户上传了图片，图片中核心主体是："+entity)
                        .advisors(a->a.param(ChatMemory.CONVERSATION_ID,finalSessionId))
                        .call().content();
                System.out.println("用户上传了图片，图片中核心主体是："+entity);
            }

            String finalSystemPrompt;
            if(useRag){
                String rewrite = rewritePrompt.formatted(question);
                String rewriteQuestion = memoryChatClient.prompt().user(rewrite)
                        .advisors(a->a.param(ChatMemory.CONVERSATION_ID,finalSessionId))
                        .call().content();
                System.out.println("用户问题重写后："+rewriteQuestion);
                String docs = ragSearchService.searchDocuments(rewriteQuestion);
                if(docs==null||docs.isEmpty()){
                    docs = "此问题本地知识库未收入";
                }
                String pathPrompt = ragPersonMap.getOrDefault(type, ragPersonMap.get("default"));
                finalSystemPrompt = pathPrompt.formatted(docs);

            }
            else{
                finalSystemPrompt = chatPersonMap.getOrDefault(type, chatPersonMap.get("default"));

            }

            StringBuilder fullAiResponse = new StringBuilder();

             return memoryChatClient.prompt().system(finalSystemPrompt).
                    user(question).advisors(a->a.param(ChatMemory.CONVERSATION_ID,finalSessionId))
                    .stream().content().map(a->{
                        fullAiResponse.append(a);
                        return a.replace("\n","").replace("\r","")
                                .replace("\t","").trim();
                    }).doOnComplete(()->{
                         try {
                             ChatHistory chatHistory = new ChatHistory();
                             chatHistory.setUserId(currentUserId);
                             chatHistory.setSessionId(finalSessionId);
                             chatHistory.setUserMessage(question);
                             chatHistory.setAiMessage(fullAiResponse.toString());
                             chatHistory.setCreateTime(LocalDateTime.now());
                             chatHistoryMapper.insert(chatHistory);
                             System.out.println("聊天记录成功存入数据库");
                         } catch (Exception e) {
                             System.err.println("聊天记录存入数据库失败："+e.getMessage());
                         }
                     });

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
}
