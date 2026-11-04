package org.example.redpic.service.impl;

import org.example.redpic.service.VisionService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class VisionServiceImpl implements VisionService {
    private final ChatClient visionChatClient;
    public VisionServiceImpl(@Qualifier("visionChatClient") ChatClient visionChatClient) {
        this.visionChatClient = visionChatClient;
    }


    @Override
    public String analyzeImage(MultipartFile file, String question) {
        try {
            Resource imageResource = new
                    ByteArrayResource(file.getBytes());
            String contentType = file.getContentType() == null ? "image/jpeg" : file.getContentType();
            String rawtext = visionChatClient.prompt()
                    .user(u->u.text(question)
                            .media(MimeType.valueOf(contentType), imageResource))
                    .call()
                    .content();
            return rawtext.replace("\n", "")
                    .replace("**", "")
                    .replaceAll("\\s+", " ")
                    .trim();
        } catch (IOException e) {
            return "分析图片失败";
               }

    }
}
