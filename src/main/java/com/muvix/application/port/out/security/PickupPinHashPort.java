package com.muvix.application.port.out.security;

public interface PickupPinHashPort {
    String hash(String rawPin);
    boolean matches(String rawPin, String encodedPin);
}
