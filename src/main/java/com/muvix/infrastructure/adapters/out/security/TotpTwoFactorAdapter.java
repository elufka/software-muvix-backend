package com.muvix.infrastructure.adapters.out.security;

import com.muvix.application.port.out.security.TwoFactorProviderPort;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

@Component
public class TotpTwoFactorAdapter implements TwoFactorProviderPort {

    private static final String BASE32 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final String ISSUER = "Muvix";
    private static final int CODE_DIGITS = 6;
    private static final long STEP_SECONDS = 30;
    private static final int RECOVERY_CODE_COUNT = 10;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final Clock clock;

    public TotpTwoFactorAdapter(Clock clock) {
        this.clock = clock;
    }

    @Override
    public String generateSecret() {
        byte[] secret = new byte[20];
        RANDOM.nextBytes(secret);
        return encodeBase32(secret);
    }

    @Override
    public String createProvisioningUri(String email, String secret) {
        String label = URLEncoder.encode(ISSUER + ":" + email, StandardCharsets.UTF_8).replace("+", "%20");
        String encodedIssuer = URLEncoder.encode(ISSUER, StandardCharsets.UTF_8).replace("+", "%20");
        return "otpauth://totp/" + label + "?secret=" + secret + "&issuer=" + encodedIssuer
                + "&algorithm=SHA1&digits=" + CODE_DIGITS + "&period=" + STEP_SECONDS;
    }

    @Override
    public boolean verifyCode(String secret, String code) {
        if (secret == null || code == null || !code.matches("\\d{" + CODE_DIGITS + "}")) return false;
        long currentCounter = clock.instant().getEpochSecond() / STEP_SECONDS;
        byte[] supplied = code.getBytes(StandardCharsets.US_ASCII);
        for (long offset = -1; offset <= 1; offset++) {
            byte[] expected = codeForCounter(secret, currentCounter + offset).getBytes(StandardCharsets.US_ASCII);
            if (MessageDigest.isEqual(expected, supplied)) return true;
        }
        return false;
    }

    @Override
    public List<String> generateRecoveryCodes() {
        List<String> codes = new ArrayList<>(RECOVERY_CODE_COUNT);
        for (int i = 0; i < RECOVERY_CODE_COUNT; i++) {
            byte[] bytes = new byte[9];
            RANDOM.nextBytes(bytes);
            String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes).toUpperCase(Locale.ROOT);
            codes.add(encoded.substring(0, 12));
        }
        return List.copyOf(codes);
    }

    static String codeForCounter(String secret, long counter) {
        try {
            byte[] key = decodeBase32(secret);
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key, "HmacSHA1"));
            byte[] hash = mac.doFinal(ByteBuffer.allocate(Long.BYTES).putLong(counter).array());
            int offset = hash[hash.length - 1] & 0x0f;
            int binary = ((hash[offset] & 0x7f) << 24)
                    | ((hash[offset + 1] & 0xff) << 16)
                    | ((hash[offset + 2] & 0xff) << 8)
                    | (hash[offset + 3] & 0xff);
            int otp = binary % 1_000_000;
            return String.format(Locale.ROOT, "%06d", otp);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Could not generate a TOTP code", ex);
        }
    }

    private static String encodeBase32(byte[] input) {
        StringBuilder output = new StringBuilder((input.length * 8 + 4) / 5);
        int buffer = 0;
        int bitsLeft = 0;
        for (byte value : input) {
            buffer = (buffer << 8) | (value & 0xff);
            bitsLeft += 8;
            while (bitsLeft >= 5) {
                output.append(BASE32.charAt((buffer >> (bitsLeft - 5)) & 0x1f));
                bitsLeft -= 5;
            }
        }
        if (bitsLeft > 0) output.append(BASE32.charAt((buffer << (5 - bitsLeft)) & 0x1f));
        return output.toString();
    }

    private static byte[] decodeBase32(String value) {
        String normalized = value.replace("=", "").toUpperCase(Locale.ROOT);
        ByteBuffer output = ByteBuffer.allocate(normalized.length() * 5 / 8);
        int buffer = 0;
        int bitsLeft = 0;
        for (char character : normalized.toCharArray()) {
            int index = BASE32.indexOf(character);
            if (index < 0) throw new IllegalArgumentException("Invalid TOTP secret");
            buffer = (buffer << 5) | index;
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                output.put((byte) ((buffer >> (bitsLeft - 8)) & 0xff));
                bitsLeft -= 8;
            }
        }
        byte[] result = new byte[output.position()];
        output.flip();
        output.get(result);
        return result;
    }
}
