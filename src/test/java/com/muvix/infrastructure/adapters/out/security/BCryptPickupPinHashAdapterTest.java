package com.muvix.infrastructure.adapters.out.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class BCryptPickupPinHashAdapterTest {
    @Test
    void hashesAndVerifiesPinWithoutStoringPlainValue() {
        BCryptPickupPinHashAdapter adapter = new BCryptPickupPinHashAdapter(new BCryptPasswordEncoder(4));

        String hash = adapter.hash("123456");

        assertNotEquals("123456", hash);
        assertTrue(adapter.matches("123456", hash));
        assertFalse(adapter.matches("654321", hash));
    }
}
