package org.example.redpic.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("redpic_user")
public class SysUser {
    @TableId(value = "id",type = IdType.AUTO)
    private Long userId;
    private String username;
    private String password;
    private LocalDateTime createTime;



}
