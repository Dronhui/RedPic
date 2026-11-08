package org.example.redpic.service.impl;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.redpic.entity.SysUser;
import org.example.redpic.exception.BizException;
import org.example.redpic.mapper.SysUserMapper;
import org.example.redpic.service.AuthService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {
    private final SysUserMapper sysUserMapper;
    public AuthServiceImpl(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(String username, String password) {
        long count = sysUserMapper.selectCount(new QueryWrapper<SysUser>().eq("username",username));
        if(count>0){
            throw new BizException("用户名已存在");
        }
        SysUser sysUser = new SysUser();
        sysUser.setUsername(username);
        sysUser.setPassword(BCrypt.hashpw(password,BCrypt.gensalt()));
        sysUser.setCreateTime(LocalDateTime.now());
        sysUserMapper.insert(sysUser);
    }

    @Override
    public SysUser getUserInfo() {
        String userId = StpUtil.getLoginIdAsString();
        SysUser sysUser = sysUserMapper.selectById(userId);
        sysUser.setPassword(null);
        return sysUser;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserPassword(String newPassword, String oldPassword) {
        String userId = StpUtil.getLoginIdAsString();
        SysUser sysUser = sysUserMapper.selectById(userId);
        if(!BCrypt.checkpw(oldPassword,sysUser.getPassword())){
            throw new BizException("旧密码错误");
        }
        sysUser.setPassword(BCrypt.hashpw(newPassword,BCrypt.gensalt()));
        sysUserMapper.updateById(sysUser);
        StpUtil.logout(userId);

    }

    @Override
    public String login(String username, String password) {
        SysUser user = sysUserMapper.selectOne(new QueryWrapper<SysUser>().eq("username",username));
        if(user ==null||!BCrypt.checkpw(password,user.getPassword())){
            throw new BizException("用户名或密码错误");
        }
        StpUtil.login(user.getUserId());
        return StpUtil.getTokenValue();
    }

}
