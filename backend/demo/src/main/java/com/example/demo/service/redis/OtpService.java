package com.example.demo.service.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class OtpService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String PREFIX = "reset_code:";

    // Сохраняем код на 5 минут
    public void saveCode(String email, String code) {
        redisTemplate.opsForValue().set(PREFIX + email, code, 15, TimeUnit.MINUTES);
    }

    // Получаем код
    public String getCode(String email) {
        return redisTemplate.opsForValue().get(PREFIX + email);
    }

    // Удаляем (после успешной проверки)
    public void deleteCode(String email) {
        redisTemplate.delete(PREFIX + email);
    }
}
