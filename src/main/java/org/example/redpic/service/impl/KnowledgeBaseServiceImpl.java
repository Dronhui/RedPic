package org.example.redpic.service.impl;

import org.example.redpic.service.KnowledgeBaseService;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.util.List;

@Service
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService {
    private final VectorStore heroesVectorStore;
    private final VectorStore sitesVectorStore;
    private final VectorStore storiesVectorStore;


    public KnowledgeBaseServiceImpl(@Qualifier("heroesVectorStore") VectorStore heroesVectorStore,
                                    @Qualifier("sitesVectorStore") VectorStore sitesVectorStore,
                                    @Qualifier("storiesVectorStore") VectorStore storiesVectorStore) {
        this.heroesVectorStore = heroesVectorStore;
        this.sitesVectorStore = sitesVectorStore;
        this.storiesVectorStore = storiesVectorStore;

    }
    @Override
    public void importDocument(MultipartFile file) {
        try {
            String fileName = file.getOriginalFilename();
            if (fileName==null || fileName.equals("")) {
                throw new RuntimeException("文件名不能为空");
            }
            String category;
            VectorStore typeVectorStore ;
            if (fileName.contains("general")) {
                category = "heroes";
                typeVectorStore = heroesVectorStore;
            }
            else if (fileName.contains("sites")) {
                category = "sites";
                typeVectorStore = sitesVectorStore;
            }
            else if (fileName.contains("stories")) {
                category = "stories";
                typeVectorStore = storiesVectorStore;
            }
            else {
                throw new RuntimeException("文件名格式错误，请确保文件名包含 generals/sites/stories！");
            }
            var resource = new InputStreamResource(file.getInputStream());
            TextReader reader = new TextReader(resource);
            List<Document> documents = reader.get();
            TokenTextSplitter splitter = new TokenTextSplitter();
            List<Document> splitDocuments = splitter.apply(documents);
            typeVectorStore.add(splitDocuments);
            if(typeVectorStore instanceof SimpleVectorStore simpleVectorStore) {
                simpleVectorStore.save(new File("vector_"+category+".json"));
                System.out.println("====== ✅ 成功保存到本地 vector_" + category + ".json ======");
            }
            System.out.println("====== ✅ 成功向 [" + category + "] 知识库导入了 " + splitDocuments.size() + " 个文本块！ ======");
        } catch (Exception e) {
            throw new RuntimeException("文件导入失败：" + e.getMessage(), e);
        }


    }
}
