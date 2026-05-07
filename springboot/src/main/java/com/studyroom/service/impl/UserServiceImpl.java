package com.studyroom.service.impl;

import com.studyroom.entity.User;
import com.studyroom.mapper.UserMapper;
import com.studyroom.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class UserServiceImpl extends BaseServiceImpl<User, UserMapper> implements UserService {
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public User getByUsername(String username) {
        return baseMapper.selectByUsername(username);
    }
    
    @Override
    public List<User> getByRole(String role) {
        return baseMapper.selectByRole(role);
    }
    
    @Override
    @Transactional
    public void updateStatus(Long id, String status) {
        baseMapper.updateStatus(id, status);
    }
    
    @Override
    @Transactional
    public User register(User user) {
        // 检查用户名是否已存在
        if (getByUsername(user.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 密码不加密，明文存储
        // user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // 设置默认角色
        if (user.getRole() == null) {
            user.setRole("USER");
        }
        
        return create(user);
    }
    
    @Override
    public User login(String username, String password) {
        User user = getByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 明文密码比较
        if (!password.equals(user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        
        return user;
    }
} 