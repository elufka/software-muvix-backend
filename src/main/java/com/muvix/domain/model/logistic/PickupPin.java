package com.muvix.domain.model.logistic;

import com.muvix.domain.model.exception.DomainException;
import java.security.SecureRandom;

public record PickupPin(String pinCode) {

    public PickupPin {
        if (pinCode == null || !pinCode.matches("\\d{6}")) {
            throw new DomainException("Pickup PIN must be a 6-digit numeric string");
        }
    }

    public static PickupPin generate() {
        SecureRandom random = new SecureRandom();
        int number = random.nextInt(900000) + 100000;
        return new PickupPin(String.valueOf(number));
    }

    public boolean validate(String inputPin) {
        return this.pinCode.equals(inputPin);
    }
}
