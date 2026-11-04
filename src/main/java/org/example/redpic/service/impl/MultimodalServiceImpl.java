package org.example.redpic.service.impl;

import org.example.redpic.service.MultimodalService;
import org.example.redpic.service.RagChatService;
import org.example.redpic.service.VisionService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.UUID;

@Service
public class MultimodalServiceImpl implements MultimodalService {
    private final RagChatService ragChatService;
    private final VisionService visionService;
    private final ChatClient memoryChatClient;
    private final String rewriteTemplate;

    public MultimodalServiceImpl(RagChatService ragChatService,
                                 VisionService visionService,
                                 @Qualifier("memoryChatClient") ChatClient memoryChatClient) {
        this.ragChatService = ragChatService;
        this.visionService = visionService;
        this.memoryChatClient = memoryChatClient;
        try {
            ClassPathResource gpsPath = new ClassPathResource("prompts/rewrite.st");
            this.rewriteTemplate = new String(gpsPath.getInputStream()
                    .readAllBytes(),"UTF-8");
        } catch (IOException e) {
            throw new RuntimeException("初始化失败：无法读取 rewrite.st 模板文件",e);
        }
    }
    @Override
    public String chatWithImage(MultipartFile image, String question,String inputSessionId) {
        String sessionId = (inputSessionId == null || inputSessionId.isBlank()) ?
                UUID.randomUUID().toString() : inputSessionId;
        if(image!=null&&!image.isEmpty()){
            String visionPrompt = " 请仔细观察图片，直接输出图片中的核心主体名称（如果是建筑或遗址，输出遗址名称；如果是人物，输出人物姓名）。不要任何废话和标点符号。";
            String imageAnalysis = visionService.analyzeImage(image, visionPrompt);
            System.out.println("========图片分析结果：" + imageAnalysis+"========");
            memoryChatClient.prompt()
                    .user("我刚刚上传了一张图片，图片中的核心实体是："+imageAnalysis)
                    .advisors(a->a.param(ChatMemory.CONVERSATION_ID,sessionId))
                    .call()
                    .content();
        }
        String rewritePrompt = rewriteTemplate.formatted(question);
        String rewrittenQuestion = memoryChatClient.prompt()
                .user(rewritePrompt)
                .advisors(a->a.param(ChatMemory.CONVERSATION_ID,sessionId))
                .call()
                .content();
        System.out.println("========重写后的检索问题：" + rewrittenQuestion+"========");
        return ragChatService.chatWithRag("请根据材料回答问题："+rewrittenQuestion);


    }
}
