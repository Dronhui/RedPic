package org.example.redpic.service;

import org.springframework.web.multipart.MultipartFile;

public interface KnowledgeBaseService {
    void importDocument(MultipartFile file);
}
