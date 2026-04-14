package com.apmosys.employeeportal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OtpRateLimitConfig {

    @Value("${otp.max.failed.attempts}")
    private int maxFailedAttempts;

    @Value("${otp.cooldown.minutes}")
    private int cooldownMinutes;

    @Value("${otp.max.requests.per.hour}")
    private int maxRequestsPerHour;

    public int getMaxFailedAttempts() {
        return maxFailedAttempts;
    }

    public int getCooldownMinutes() {
        return cooldownMinutes;
    }

    public int getMaxRequestsPerHour() {
        return maxRequestsPerHour;
    }
}
