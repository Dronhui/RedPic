package org.example.redpic.controller;

import cn.dev33.satoken.stp.StpUtil;
import org.example.redpic.common.Result;
import org.example.redpic.entity.SysUser;
import org.example.redpic.service.AuthService;
import org.springframework.web.bind.annotation.GetMapping;
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
        return Result.success(token);
    }
    @PostMapping("/logout")
    public Result<String> logout(){
        StpUtil.logout();
        return Result.success("退出成功，同志，期待下次相遇！");
    }

    @GetMapping("/check")
    public Result<String> check(){
        return Result.success(StpUtil.getLoginIdAsString());
    }

    @GetMapping("/info")
    public Result<SysUser> info(){
        return Result.success(authService.getUserInfo());
    }
    @PostMapping("/updatePassword")
    public Result<String> updatePassword(@RequestParam("newPassword") String newPassword, @RequestParam("oldPassword") String oldPassword){
        authService.updateUserPassword(newPassword,oldPassword);
        return Result.success("密码修改成功，请重新登陆！");

    }

}