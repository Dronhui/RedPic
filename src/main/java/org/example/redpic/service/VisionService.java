package org.example.redpic.service;

import org.springframework.web.multipart.MultipartFile;

public interface VisionService {
    String analyzeImage(MultipartFile file, String question);


}
