package org.example.redpic.service.impl;

import org.example.redpic.service.KnowledgeBaseService;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Service
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService {
    private final VectorStore vectorStore;
    public KnowledgeBaseServiceImpl(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }
    @Override
    public void importDocument(MultipartFile file) {

        try {
            var resource = new InputStreamResource(file.getInputStream());
            TextReader textReader = new TextReader(resource);
            List<Document> documents = textReader.get();
            TokenTextSplitter splitter = new TokenTextSplitter();
            List<Document>  splitDocuments = splitter.apply(documents);
            vectorStore.add( splitDocuments);
            System.out.println("====== 成功导入了 " +
                    splitDocuments.size() + " 个文本块！ ======");
        } catch (Exception e) {
            throw new RuntimeException("文档导入失败", e);
        }


    }
}
