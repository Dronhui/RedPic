package org.example.redpic.config;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.io.File;

@Configuration
public class VectorStoreConfig {

    @Bean("heroesVectorStore")
    public VectorStore heroesVectorStore(@Qualifier("zhiPuAiEmbeddingModel") EmbeddingModel embeddingModel) {
        return loadStore(embeddingModel,"vector_heroes.json");
    }
    @Bean("sitesVectorStore")
    public VectorStore sitesVectorStore(@Qualifier("zhiPuAiEmbeddingModel") EmbeddingModel embeddingModel) {
        return loadStore(embeddingModel,"vector_sites.json");
    }
    @Bean("storiesVectorStore")
    public VectorStore storiesVectorStore(@Qualifier("zhiPuAiEmbeddingModel") EmbeddingModel embeddingModel) {
        return loadStore(embeddingModel,"vector_stories.json");
    }
    private VectorStore loadStore(EmbeddingModel embeddingModel,String fileName) {
        SimpleVectorStore store = SimpleVectorStore.builder(embeddingModel).build();
        File file = new File(fileName);
        if (file.exists()) {
            store.load(file);
            System.out.println("====== 本地知识库加载成功！ ======");
        }
        return store;
    }


}
