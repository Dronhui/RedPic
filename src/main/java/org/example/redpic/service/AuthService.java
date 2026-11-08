package org.example.redpic.service;

import org.example.redpic.entity.SysUser;

public interface AuthService {
    String login(String username, String password);
    void register(String username, String password);
    SysUser getUserInfo();
    void updateUserPassword(String newPassword, String oldPassword);
}
