package org.example.redpic.service;

public interface AuthService {
    String login(String username, String password);
    void register(String username, String password);
}
