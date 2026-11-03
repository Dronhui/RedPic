package org.example.redpic.service.impl;

import org.example.redpic.service.MultimodalService;
import org.example.redpic.service.RagChatService;
import org.example.redpic.service.VisionService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public class MultimodalServiceImpl implements MultimodalService {
    private final RagChatService ragChatService;
    private final VisionService visionService;
    private final ChatClient memoryChatClient;

    public MultimodalServiceImpl(RagChatService ragChatService,
                                 VisionService visionService,
                                 DeepSeekChatModel deepSeekChatModel,
                                 MessageWindowChatMemory aiChatMemory) {
        this.ragChatService = ragChatService;
        this.visionService = visionService;
        this.memoryChatClient = ChatClient.builder(deepSeekChatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(aiChatMemory).build())
                .build();
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
        String rewritePrompt = """
                 你是一个智能检索助手。请根据历史对话，将用户的问题改写为一个包含具体地名的独立问题。
                 如果用户问题包含了代词（如“那里”、“这里”），请结合历史对话中的实体进行替换。
                 直接输出改写后的问题，绝对不要输出任何废话。
                 用户问题：%s
                """.formatted(question);
        String rewrittenQuestion = memoryChatClient.prompt()
                .user(rewritePrompt)
                .advisors(a->a.param(ChatMemory.CONVERSATION_ID,sessionId))
                .call()
                .content();
        System.out.println("========重写后的检索问题：" + rewrittenQuestion+"========");
        return ragChatService.chatWithRag("请根据材料回答问题："+rewrittenQuestion);


    }
}
