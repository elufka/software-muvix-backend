package com.muvix.infrastructure.adapters.in.rest.controller;

import com.muvix.application.usecase.transaction.OpenDisputeService;
import com.muvix.application.usecase.transaction.ProcessMobbexWebhookService;
import com.muvix.application.usecase.transaction.RefundOrderService;
import com.muvix.application.usecase.transaction.ReleaseEscrowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments/mobbex")
public class MobbexEscrowController {

    private final ProcessMobbexWebhookService webhookService;
    private final ReleaseEscrowService releaseEscrowService;
    private final OpenDisputeService openDisputeService;
    private final RefundOrderService refundOrderService;

    public MobbexEscrowController(ProcessMobbexWebhookService webhookService,
                                  ReleaseEscrowService releaseEscrowService,
                                  OpenDisputeService openDisputeService,
                                  RefundOrderService refundOrderService) {
        this.webhookService = webhookService;
        this.releaseEscrowService = releaseEscrowService;
        this.openDisputeService = openDisputeService;
        this.refundOrderService = refundOrderService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(@RequestBody Map<String, Object> payload) {
        webhookService.processWebhook(payload);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/orders/{id}/liberar")
    public ResponseEntity<String> releaseFunds(@PathVariable Long id, @RequestParam String mobbexId) {
        releaseEscrowService.releaseFunds(id, mobbexId);
        return ResponseEntity.ok("Fondos liberados exitosamente al vendedor.");
    }

    @PostMapping("/orders/{id}/reembolsar")
    public ResponseEntity<String> refundOrder(@PathVariable Long id, @RequestParam String mobbexId) {
        refundOrderService.refundOrder(id, mobbexId);
        return ResponseEntity.ok("Dinero reembolsado con éxito al comprador.");
    }
}
