package com.muvix.application.port.out.security;

import java.util.List;

public interface TwoFactorProviderPort {

    String generateSecret();

    String createProvisioningUri(String email, String secret);

    boolean verifyCode(String secret, String code);

    List<String> generateRecoveryCodes();
}
