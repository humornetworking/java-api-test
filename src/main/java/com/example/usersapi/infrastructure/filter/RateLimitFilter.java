package com.example.usersapi.infrastructure.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Sliding-window rate limiter (no external dependency).
 * Each client IP gets an independent request timestamp queue.
 * Requests older than the window are evicted on each check.
 */
@Component
@Order(1)
public class RateLimitFilter extends OncePerRequestFilter {

    @Value("${app.rate-limit.capacity:20}")
    private int capacity;

    @Value("${app.rate-limit.refill-duration-seconds:60}")
    private long windowSeconds;

    private final Map<String, Queue<Long>> requestLog = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String clientIp = resolveClientIp(request);
        long now = System.currentTimeMillis();
        long windowStart = now - (windowSeconds * 1_000);

        Queue<Long> timestamps = requestLog.computeIfAbsent(clientIp, k -> new ConcurrentLinkedQueue<>());
        timestamps.removeIf(t -> t < windowStart);

        if (timestamps.size() >= capacity) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"mensaje\": \"Demasiadas solicitudes. Por favor, intente más tarde.\"}");
            return;
        }

        timestamps.add(now);
        filterChain.doFilter(request, response);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
