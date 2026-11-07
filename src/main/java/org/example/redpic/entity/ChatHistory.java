package org.example.redpic.entity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_history")
public class ChatHistory {
    private Long id;
    private String user;
    private String sessionId;
    private String usermessage;
    private String aimessage;
    private LocalDateTime createTime;

}
