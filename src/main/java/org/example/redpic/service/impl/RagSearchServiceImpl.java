package org.example.redpic.service.impl;
import org.example.redpic.service.RagSearchService;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RagSearchServiceImpl implements RagSearchService {
    private final VectorStore vectorStore;
    public RagSearchServiceImpl(VectorStore vectorStore) {
        this.vectorStore = vectorStore;

    }

    @Override
    public String searchDocuments(String question) {
        List<Document> docs = vectorStore.similaritySearch(
                SearchRequest.builder().query(question).topK(10).build());
        String content = docs.stream().map(Document::getText)
                .collect(Collectors.joining("\n"));

        if(content == null||content.isEmpty()){
            return "抱歉大模型为空";
        }
        return content;

    }
}
