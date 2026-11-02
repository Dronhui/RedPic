package org.example.redpic.service.impl;

import org.example.redpic.service.RagChatService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RagChatServiceImpl implements RagChatService {
    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    public RagChatServiceImpl(ChatClient chatClient, VectorStore vectorStore) {
        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
    }

    @Override
    public String chatWithRag(String question) {
        List<Document> docs = vectorStore.similaritySearch(
                SearchRequest.builder().query(question).topK(3).build());
        String content = docs.stream().map(Document::getText)
                .collect(Collectors.joining("\n"));
        String systemPrompt =  """
                你是一个严谨的红色文化专家。
                请严格根据下面提供的【参考资料】来回答用户的问题。
                如果【参考资料】中没有相关答案，请直接回答“根据现有资料，我无法回答该问题”，绝不胡编乱造。
                
                格式要求：
                1. 绝对禁止使用任何 Markdown 排版符号（如 **、#、- 等）。
                2. 绝对禁止使用 1. 2. 3. 这种数字列表。
                3. 请直接输出一段连贯的自然语言文本，用中文逗号和句号连接即可。
                
                【参考资料】：
                """ + content;
        return chatClient.prompt().system(systemPrompt).user(question).call().content();

    }
}
