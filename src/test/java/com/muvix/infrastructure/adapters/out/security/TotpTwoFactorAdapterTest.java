package com.muvix.infrastructure.adapters.out.security;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TotpTwoFactorAdapterTest {

    @Test
    void verifiesRfc6238Sha1CodeAtTheCurrentTimeStep() {
        Clock clock = Clock.fixed(Instant.ofEpochSecond(59), ZoneOffset.UTC);
        TotpTwoFactorAdapter adapter = new TotpTwoFactorAdapter(clock);
        String secret = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ";

        assertTrue(adapter.verifyCode(secret, "287082"));
        assertFalse(adapter.verifyCode(secret, "000000"));
    }

    @Test
    void createsDistinctSingleUseRecoveryCodesAndProvisioningUri() {
        TotpTwoFactorAdapter adapter = new TotpTwoFactorAdapter(Clock.systemUTC());

        var codes = adapter.generateRecoveryCodes();
        String uri = adapter.createProvisioningUri("buyer@example.test", "SECRET");

        assertEquals(10, codes.size());
        assertEquals(10, new HashSet<>(codes).size());
        assertTrue(uri.startsWith("otpauth://totp/"));
        assertTrue(uri.contains("issuer=Muvix"));
    }
}
