package com.muvix.infrastructure.adapters.out.security;

import com.muvix.application.port.out.security.PickupPinHashPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptPickupPinHashAdapter implements PickupPinHashPort {
    private final PasswordEncoder passwordEncoder;

    public BCryptPickupPinHashAdapter(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override public String hash(String rawPin) { return passwordEncoder.encode(rawPin); }

    @Override public boolean matches(String rawPin, String encodedPin) {
        return rawPin != null && encodedPin != null && passwordEncoder.matches(rawPin, encodedPin);
    }
}