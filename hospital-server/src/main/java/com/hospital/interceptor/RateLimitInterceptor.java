package com.hospital.interceptor;

import com.hospital.common.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final Map<String, WindowEntry> counter = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String ip = getClientIp(request);
        long now = System.currentTimeMillis();
        WindowEntry entry = counter.compute(ip, (k, v) -> {
            if (v == null || now - v.timestamp > 1000) {
                return new WindowEntry(now);
            }
            v.count.incrementAndGet();
            return v;
        });
        if (entry.count.get() > 20) {
            throw new BusinessException(429, "请求过于频繁，请稍后重试");
        }
        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    private static class WindowEntry {
        final long timestamp;
        final AtomicInteger count = new AtomicInteger(1);

        WindowEntry(long timestamp) {
            this.timestamp = timestamp;
        }
    }
}
