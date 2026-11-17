
import com.muvix.application.port.in.transaction.ProcessMobbexWebhookUseCase;
import com.muvix.application.port.in.transaction.ReleaseEscrowUseCase;
import com.muvix.application.port.in.transaction.RefundOrderUseCase;
import com.muvix.application.port.in.transaction.GetEscrowUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/escrow")
public class EscrowAccountController {

    private final ProcessMobbexWebhookUseCase processMobbexWebhookUseCase;
    private final ReleaseEscrowUseCase releaseEscrowUseCase;
    private final RefundOrderUseCase refundOrderUseCase;
    private final GetEscrowUseCase getEscrowUseCase;

    public EscrowAccountController(
            ProcessMobbexWebhookUseCase processMobbexWebhookUseCase,
            ReleaseEscrowUseCase releaseEscrowUseCase,
            RefundOrderUseCase refundOrderUseCase, GetEscrowUseCase getEscrowUseCase
    ) {
        this.processMobbexWebhookUseCase = processMobbexWebhookUseCase;
        this.releaseEscrowUseCase = releaseEscrowUseCase;
        this.refundOrderUseCase = refundOrderUseCase;
        this.getEscrowUseCase = getEscrowUseCase;
    }

    @PostMapping("/webhook/mobbex")
    public ResponseEntity<Void> handleMobbexWebhook(
            @RequestBody Map<String, Object> payload
    ) {
        processMobbexWebhookUseCase.processWebhook(payload);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/orders/{orderId}/release")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<Void> releaseEscrow(
            @PathVariable Long orderId,
            @RequestParam String mobbexOperationId
    ) {
        releaseEscrowUseCase.releaseFunds(orderId, mobbexOperationId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<com.muvix.domain.model.transaction.EscrowAccount> getEscrow(@AuthenticationPrincipal Jwt jwt,@PathVariable Long orderId){
        java.util.List<String> roles=jwt.getClaimAsStringList("roles");
        boolean moderator=roles!=null&&roles.contains("MODERATOR");
        return ResponseEntity.ok(getEscrowUseCase.getForOrder(orderId,Long.valueOf(jwt.getSubject()),moderator));
    }

    @PostMapping("/orders/{orderId}/refund")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<Void> refundOrder(
            @PathVariable Long orderId,
            @RequestParam String mobbexOperationId
    ) {
        refundOrderUseCase.refundOrder(orderId, mobbexOperationId);
        return ResponseEntity.ok().build();
    }
}
