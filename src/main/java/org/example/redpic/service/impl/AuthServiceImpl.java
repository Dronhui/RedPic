package org.example.redpic.service.impl;

import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.redpic.entity.SysUser;
import org.example.redpic.mapper.SysUserMapper;
import org.example.redpic.service.AuthService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {
    private SysUserMapper sysUserMapper;
    public AuthServiceImpl(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(String username, String password) {
        long count = sysUserMapper.selectCount(new QueryWrapper<SysUser>().eq("username",username));
        if(count>0){
            throw new RuntimeException("用户名已存在");
        }
        SysUser sysUser = new SysUser();
        sysUser.setUsername(username);
        sysUser.setPassword(SaSecureUtil.md5(password));
        sysUser.setCreateTime(LocalDateTime.now());
        sysUserMapper.insert(sysUser);
    }

    @Override
    public String login(String username, String password) {
        SysUser user = sysUserMapper.selectOne(new QueryWrapper<SysUser>().eq("username",username));
        if(user ==null||!user.getPassword().equals(SaSecureUtil.md5(password))){
            throw new RuntimeException("用户名或密码错误");
        }
        StpUtil.login(user.getId());
        return StpUtil.getTokenValue();


    }
}
