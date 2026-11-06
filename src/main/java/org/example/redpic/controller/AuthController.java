package org.example.redpic.controller;

import org.example.redpic.common.Result;
import org.example.redpic.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    @PostMapping("/register")
    public Result<String> register(@RequestParam("username") String username, @RequestParam("password") String password){
        authService.register(username,password);
        return Result.success("注册成功，同志");
    }
    @PostMapping("/login")
    public Result<String> login(@RequestParam("username") String username, @RequestParam("password") String password){
        String token = authService.login(username,password);
        return Result.success("登录成功，你的token是："+token);
    }

}
