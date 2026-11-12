package com.muvix.application.port.in.transaction;

import java.util.Map;

public interface ProcessMobbexWebhookUseCase {
    void processWebhook(Map<String, Object> payload);
}
