package com.event.otg_backend.services.impl;

import com.event.otg_backend.exceptions.otp.OtpExpiredException;
import com.event.otg_backend.exceptions.otp.OtpInvalidException;
import com.event.otg_backend.exceptions.otp.OtpMaxAttemptsExceededException;
import com.event.otg_backend.exceptions.otp.OtpRateLimitExceededException;
import com.event.otg_backend.helpers.security.OtpGenerator;
import com.event.otg_backend.helpers.security.OtpHasher;
import com.event.otg_backend.services.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl  implements OtpService {

    private final RedisTemplate<String, String> redisTemplate;
    private final OtpHasher otpHasher;

    @Value("${spring.app.otp.expiry-minutes}")
    private long otpExpiryMinutes;

    @Value("${spring.app.otp.max-requests-per-window}")
    private int maxRequestsPerWindow;

    @Value("${spring.app.otp.request-window-minutes}")
    private long requestWindowMinutes;

    @Value("${spring.app.otp.max-verify-attempts}")
    private int maxVerifyAttempts;

    private String otpKey(String email){
        return "otp:" + email;
    }

    private String attemptKey(String email){
        return "otp_attempts:" + email;
    }

    private String rateLimitKey(String email){
        return "otp_rate:" + email;
    }


    @Override
    public String generateAndStoreOtp(String email) {

        String rateKey = rateLimitKey(email);
        Long requestCount = redisTemplate.opsForValue().increment(rateKey);

        if(requestCount != null && requestCount == 1L){
            redisTemplate.expire(rateKey, Duration.ofMinutes(requestWindowMinutes));
        }

        if(requestCount != null && requestCount > maxRequestsPerWindow){
            throw new OtpRateLimitExceededException();
        }

        String otp = OtpGenerator.generate();
        String hashedOtp = otpHasher.hash(otp);

        redisTemplate.opsForValue().set(otpKey(email), hashedOtp, Duration.ofMinutes(otpExpiryMinutes));
        redisTemplate.delete(attemptKey(email));

        return otp;
    }

    @Override
    public void verifyOtp(String email, String submittedOtp) {

        String storedHash = redisTemplate.opsForValue().get(otpKey(email));
        if (storedHash == null){
            throw new OtpExpiredException();
        }

        String attemptStr = redisTemplate.opsForValue().get(attemptKey(email));
        int attempts = attemptStr == null ? 0 : Integer.parseInt(attemptStr);

        if (attempts >= maxVerifyAttempts) {
            redisTemplate.delete(otpKey(email));
            throw new OtpMaxAttemptsExceededException();
        }

        if (!otpHasher.matches(submittedOtp, storedHash)){
            redisTemplate.opsForValue().increment(attemptKey(email));
            redisTemplate.expire(attemptKey(email), Duration.ofMinutes(otpExpiryMinutes));
            throw new OtpInvalidException();
        }

        redisTemplate.delete(otpKey(email));
        redisTemplate.delete(attemptKey(email));
    }

    @Override
    public void invalidateOtp(String email) {
        redisTemplate.delete(otpKey(email));
        redisTemplate.delete(attemptKey(email));
    }
}
