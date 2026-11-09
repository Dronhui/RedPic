package org.example.redpic.service.impl;
import org.example.redpic.service.RagSearchService;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RagSearchServiceImpl implements RagSearchService {
    private final VectorStore heroesVectorStore;
    private final VectorStore sitesVectorStore;
    private final VectorStore storiesVectorStore;
    public RagSearchServiceImpl(@Qualifier("heroesVectorStore") VectorStore heroesVectorStore,
                                @Qualifier("sitesVectorStore") VectorStore sitesVectorStore,
                                @Qualifier("storiesVectorStore") VectorStore storiesVectorStore) {
        this.heroesVectorStore = heroesVectorStore;
        this.sitesVectorStore = sitesVectorStore;
        this.storiesVectorStore = storiesVectorStore;

    }

    @Override
    public String searchDocuments(String question) {
       SearchRequest searchRequest = SearchRequest.builder().query(question).topK(5).build();
       List<Document> docs = new ArrayList<>();
       docs.addAll(heroesVectorStore.similaritySearch(searchRequest));
       docs.addAll(sitesVectorStore.similaritySearch(searchRequest));
       docs.addAll(storiesVectorStore.similaritySearch(searchRequest));
       if (docs.isEmpty()) {
           return "";
       }
       return docs.stream().map(Document::getText)
               .collect(Collectors.joining("\n"));

    }
}
