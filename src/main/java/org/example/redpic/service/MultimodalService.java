package org.example.redpic.service;

import org.springframework.web.multipart.MultipartFile;

public interface MultimodalService {
    public String chatWithImage(MultipartFile image, String question,String inputSessionId);
}
