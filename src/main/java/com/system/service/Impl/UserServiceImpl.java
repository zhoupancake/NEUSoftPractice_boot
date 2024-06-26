package com.system.service.Impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.system.dto.User;
import com.system.mapper.UserMapper;
import com.system.service.UserService;
import org.springframework.stereotype.Service;

@Service
@DS("public")
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
