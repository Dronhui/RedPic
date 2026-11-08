package org.example.redpic.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_history")
public class ChatHistory {
    @TableId(value="id", type = IdType.AUTO)
    private Long chatHistoryId;
    @TableField(value="user_id")
    private String userId;
    private String sessionId;
    private String userMessage;
    private String aiMessage;
    private String imageUrl;
    private LocalDateTime createTime;

}
