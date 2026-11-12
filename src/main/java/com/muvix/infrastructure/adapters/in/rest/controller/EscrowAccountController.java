
import com.muvix.application.port.in.transaction.ProcessMobbexWebhookUseCase;
import com.muvix.application.port.in.transaction.ReleaseEscrowUseCase;
import com.muvix.application.port.in.transaction.RefundOrderUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/escrow")
public class EscrowAccountController {

    private final ProcessMobbexWebhookUseCase processMobbexWebhookUseCase;
    private final ReleaseEscrowUseCase releaseEscrowUseCase;
    private final RefundOrderUseCase refundOrderUseCase;

    public EscrowAccountController(
            ProcessMobbexWebhookUseCase processMobbexWebhookUseCase,
            ReleaseEscrowUseCase releaseEscrowUseCase,
            RefundOrderUseCase refundOrderUseCase
    ) {
        this.processMobbexWebhookUseCase = processMobbexWebhookUseCase;
        this.releaseEscrowUseCase = releaseEscrowUseCase;
        this.refundOrderUseCase = refundOrderUseCase;
    }

    @PostMapping("/webhook/mobbex")
    public ResponseEntity<Void> handleMobbexWebhook(
            @RequestBody Map<String, Object> payload
    ) {
        processMobbexWebhookUseCase.processWebhook(payload);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/orders/{orderId}/release")
    public ResponseEntity<Void> releaseEscrow(
            @PathVariable Long orderId,
            @RequestParam String mobbexOperationId
    ) {
        releaseEscrowUseCase.releaseFunds(orderId, mobbexOperationId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/orders/{orderId}/refund")
    public ResponseEntity<Void> refundOrder(
            @PathVariable Long orderId,
            @RequestParam String mobbexOperationId
    ) {
        refundOrderUseCase.refundOrder(orderId, mobbexOperationId);
        return ResponseEntity.ok().build();
    }
}