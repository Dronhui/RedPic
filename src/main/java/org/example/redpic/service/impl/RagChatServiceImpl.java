package org.example.redpic.service.impl;

import org.example.redpic.service.RagChatService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RagChatServiceImpl implements RagChatService {
    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final String ragPrompt;
    public RagChatServiceImpl(@Qualifier("ragChatClient") ChatClient chatClient, VectorStore vectorStore) {
        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
        try {
            ClassPathResource prspath=new ClassPathResource("prompts/rag.st");
            this.ragPrompt = new String(prspath.getInputStream().readAllBytes(),"utf-8");
        } catch (IOException e) {
            throw new RuntimeException("rag prompt not found",e);
        }


    }

    @Override
    public String chatWithRag(String question) {
        List<Document> docs = vectorStore.similaritySearch(
                SearchRequest.builder().query(question).topK(10).build());
        String content = docs.stream().map(Document::getText)
                .collect(Collectors.joining("\n"));
        String systemPrompt = ragPrompt.formatted(content);

        String Response = chatClient.prompt().system(systemPrompt).user(question).call().content();
        if(Response==null){
            return "抱歉大模型为空";
        }
        Response = Response.replace("\n", "")
                .replace("\r", "")
                .replace("**", "")
                .replaceAll("\\s+", " ")
                .trim();
        return Response;

    }
}
