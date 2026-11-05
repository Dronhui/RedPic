package org.example.redpic.service.impl;
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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
@Service
public class ChatServiceImpl implements ChatService {

        private final ChatClient memoryChatClient;
        private final RagSearchService ragSearchService;
        private final VisionService visionService;
        private final Map<String,String> chatPersonMap = new HashMap<>();
        private final Map<String,String> ragPersonMap = new HashMap<>();
        private final String rewritePrompt;
        public ChatServiceImpl(VisionService visionService, RagSearchService ragSearchService
                , @Qualifier("memoryChatClient") ChatClient memoryChatClient) {
            this.memoryChatClient = memoryChatClient;
            this.ragSearchService = ragSearchService;
            this.visionService = visionService;
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
            if(image!=null&&!image.isEmpty()){
                String entity = visionService.analyzeImage(image,"请识别主体");
                memoryChatClient.prompt().user("用户上传了图片，图片中核心主体是："+entity)
                        .advisors(a->a.param(ChatMemory.CONVERSATION_ID,finalSessionId))
                        .call().content();
            }
            String finalSystemPrompt;
            if(useRag){
                String rewrite = rewritePrompt.formatted(question);
                String rewriteQuestion = memoryChatClient.prompt().user(rewrite)
                        .advisors(a->a.param(ChatMemory.CONVERSATION_ID,finalSessionId))
                        .call().content();
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
             return memoryChatClient.prompt().system(finalSystemPrompt).
                    user(question).advisors(a->a.param(ChatMemory.CONVERSATION_ID,finalSessionId))
                    .stream().content();

        }
    }
