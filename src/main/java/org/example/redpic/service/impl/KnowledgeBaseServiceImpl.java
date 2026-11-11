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
import java.util.ArrayList;
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
            String fullText = new String(file.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);

// 2. 按左括号 "【" 劈开
            String[] sections = fullText.split("=+\\s*【");

            List<Document> documents = new ArrayList<>();
// 3. 从 1 开始循环，把名字拼回去作为独立的一块
            for (int i = 1; i < sections.length; i++) {
                documents.add(new Document("【" + sections[i].trim()));
            }

// 4. 直接入库
            typeVectorStore.add(documents);
            if(typeVectorStore instanceof SimpleVectorStore simpleVectorStore) {
                simpleVectorStore.save(new File("vector_"+category+".json"));
                System.out.println("====== ✅ 成功保存到本地 vector_" + category + ".json ======");
            }
            System.out.println("====== ✅ 成功向 [" + category + "] 知识库导入了 " + documents.size() + " 个文本块！ ======");
        } catch (Exception e) {
            throw new RuntimeException("文件导入失败：" + e.getMessage(), e);
        }


    }
}
