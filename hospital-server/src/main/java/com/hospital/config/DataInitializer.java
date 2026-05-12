package com.hospital.config;

import com.hospital.entity.User;
import com.hospital.mapper.UserMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final UserMapper userMapper;

    @PostConstruct
    public void initAdmin() {
        Long count = userMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getIdCard, "admin"));
        if (count == 0) {
            User admin = new User();
            admin.setIdCard("admin");
            admin.setPasswordHash("admin");
            admin.setName("系统管理员");
            admin.setRole(2);
            admin.setStatus(1);
            admin.setMustChangePwd(1);
            userMapper.insert(admin);
            log.info("内置admin账户已创建，初始密码: admin");
        } else {
            log.info("admin账户已存在，跳过初始化");
        }
    }
}
