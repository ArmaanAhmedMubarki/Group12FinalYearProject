package com.group12.athleticaX.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Component
public class OtpStore {

    private static final int EXPIRY_MINUTES = 5;
    private static final int COOLDOWN_MINUTES = 5;

    private static class OtpData {
        String otp;
        LocalDateTime expiry;
        LocalDateTime lastSent;

        OtpData(String otp, LocalDateTime expiry, LocalDateTime lastSent) {
            this.otp = otp;
            this.expiry = expiry;
            this.lastSent = lastSent;
        }
    }

    private final Map<String, OtpData> otpMap = new ConcurrentHashMap<>();

    // Save OTP
    public void saveOtp(String email, String otp) {
        otpMap.put(email, new OtpData(
                otp,
                LocalDateTime.now().plusMinutes(EXPIRY_MINUTES),
                LocalDateTime.now()
        ));
    }

    // Validate OTP
    public boolean validateOtp(String email, String otp) {
        OtpData data = otpMap.get(email);

        if (data == null) return false;

        if (data.expiry.isBefore(LocalDateTime.now())) {
            otpMap.remove(email);
            return false;
        }

        if (!data.otp.equals(otp)) return false;

        otpMap.remove(email);
        return true;
    }

    // 🔥 Check cooldown
    public boolean canResendOtp(String email) {
        OtpData data = otpMap.get(email);

        if (data == null) return true;

        return data.lastSent.plusMinutes(COOLDOWN_MINUTES)
                .isBefore(LocalDateTime.now());
    }

    // 🔥 Get remaining cooldown time (for UI message)
    public long getRemainingSeconds(String email) {
        OtpData data = otpMap.get(email);

        if (data == null) return 0;

        LocalDateTime nextAllowed = data.lastSent.plusMinutes(COOLDOWN_MINUTES);

        if (nextAllowed.isBefore(LocalDateTime.now())) return 0;

        return java.time.Duration.between(LocalDateTime.now(), nextAllowed).getSeconds();
    }
}