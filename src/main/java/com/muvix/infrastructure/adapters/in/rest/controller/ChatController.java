package com.muvix.infrastructure.adapters.in.rest.controller;

import com.muvix.application.port.in.logistic.ChatCoordinatorUseCase;
import com.muvix.domain.model.logistic.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.time.Instant;
import java.util.List;

@Controller
public class ChatController {
    private final ChatCoordinatorUseCase coordinator;

    public ChatController(ChatCoordinatorUseCase coordinator) {
        this.coordinator = coordinator;
    }

    @GetMapping("/api/v1/chat/orders/{orderId}/messages")
    @ResponseBody
    public List<ChatMessage> history(@AuthenticationPrincipal Jwt jwt, @PathVariable Long orderId) {
        return coordinator.history(orderId, userId(jwt));
    }

    @PostMapping("/api/v1/chat/orders/{orderId}/messages")
    @ResponseBody
    public ResponseEntity<ChatMessage> send(@AuthenticationPrincipal Jwt jwt,
                                            @PathVariable Long orderId,
                                            @Valid @RequestBody SendMessageRequest request) {
        ChatMessage m = coordinator.send(new ChatCoordinatorUseCase.SendMessage(orderId, userId(jwt), request.content(), request.templateId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(m);
    }

    @GetMapping("/api/v1/chat/offers/{offerId}/messages")
    @ResponseBody
    public List<ChatMessage> offerHistory(@AuthenticationPrincipal Jwt jwt, @PathVariable Long offerId) {
        return coordinator.offerHistory(offerId, userId(jwt));
    }

    @PostMapping("/api/v1/chat/offers/{offerId}/messages")
    @ResponseBody
    public ResponseEntity<ChatMessage> sendOfferMessage(@AuthenticationPrincipal Jwt jwt, @PathVariable Long offerId, @Valid @RequestBody SendMessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(coordinator.sendOfferMessage(offerId, userId(jwt), request.content(), request.templateId()));
    }

    @GetMapping("/api/v1/chat/offers/{offerId}/templates")
    @ResponseBody
    public List<ChatMessageTemplate> offerTemplates(@AuthenticationPrincipal Jwt jwt, @PathVariable Long offerId, @RequestParam ChatMessageTemplate.Type type) {
        coordinator.offerHistory(offerId, userId(jwt));
        return coordinator.templates(type);
    }

    @GetMapping("/api/v1/chat/orders/{orderId}/templates")
    @ResponseBody
    public List<ChatMessageTemplate> templates(@AuthenticationPrincipal Jwt jwt, @PathVariable Long orderId, @RequestParam ChatMessageTemplate.Type type) {
        coordinator.history(orderId, userId(jwt));
        return coordinator.templates(type);
    }

    @PostMapping("/api/v1/chat/orders/{orderId}/pickup-proposals")
    @ResponseBody
    public ResponseEntity<PickupCoordination> propose(@AuthenticationPrincipal Jwt jwt, @PathVariable Long orderId, @Valid @RequestBody PickupProposalRequest request) {
        var c = coordinator.proposePickupTime(new ChatCoordinatorUseCase.ProposePickupTime(orderId, userId(jwt), request.startTime(), request.endTime(), request.authorizedPerson()));
        return ResponseEntity.status(HttpStatus.CREATED).body(c);
    }

    @PostMapping("/api/v1/chat/orders/{orderId}/pickup-proposals/{proposalId}/agree")
    @ResponseBody
    public PickupCoordination agree(@AuthenticationPrincipal Jwt jwt, @PathVariable Long orderId, @PathVariable Long proposalId) {
        return coordinator.agreeToPickupTime(orderId, proposalId, userId(jwt));
    }

    @PostMapping("/api/v1/chat/orders/{orderId}/pickup-proposals/{proposalId}/confirm")
    @ResponseBody
    public PickupCoordination confirm(@AuthenticationPrincipal Jwt jwt, @PathVariable Long orderId, @PathVariable Long proposalId, @Valid @RequestBody ConfirmPickupRequest request) {
        return coordinator.confirmPickupTime(orderId, proposalId, userId(jwt), request.accepted());
    }

    @GetMapping("/api/v1/chat/admin/message-templates")
    @ResponseBody
    @PreAuthorize("hasRole('MODERATOR')")
    public List<ChatMessageTemplate> manageTemplates(){
        return coordinator.manageTemplates();
    }

    @PostMapping("/api/v1/chat/admin/message-templates")
    @ResponseBody
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<ChatMessageTemplate> createTemplate(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody TemplateRequest request){
        var created=coordinator.createTemplate(new ChatCoordinatorUseCase.CreateTemplate(request.type(),request.title(),request.message(),userId(jwt)));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/api/v1/chat/orders/{orderId}/pickup-proposals/current")
    @ResponseBody
    public java.util.Optional<PickupCoordination> currentProposal(@AuthenticationPrincipal Jwt jwt,@PathVariable Long orderId){
        return coordinator.currentPickupTime(orderId,userId(jwt));
    }

    @PutMapping("/api/v1/chat/admin/message-templates/{id}")
    @ResponseBody
    @PreAuthorize("hasRole('MODERATOR')")
    public ChatMessageTemplate updateTemplate(@AuthenticationPrincipal Jwt jwt,@PathVariable Long id,@Valid @RequestBody TemplateRequest request){
        return coordinator.updateTemplate(id,new ChatCoordinatorUseCase.CreateTemplate(request.type(),request.title(),request.message(),userId(jwt)));
    }

    @DeleteMapping("/api/v1/chat/admin/message-templates/{id}")
    @ResponseBody @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id){
        coordinator.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }

    @MessageMapping("/chat/orders/{orderId}/messages")
    public void websocketSend(@DestinationVariable Long orderId,@Payload SendMessageRequest request,Principal principal){
        coordinator.send(new ChatCoordinatorUseCase.SendMessage(orderId,Long.valueOf(principal.getName()),request.content(),request.templateId()));
    }

    @MessageMapping("/chat/offers/{offerId}/messages")
    public void websocketOfferSend(@DestinationVariable Long offerId,@Payload SendMessageRequest request,Principal principal){
        coordinator.sendOfferMessage(offerId,Long.valueOf(principal.getName()),request.content(),request.templateId());
    }

    private static Long userId(Jwt jwt){
        return Long.valueOf(jwt.getSubject());
    }
    public record SendMessageRequest(String content,Long templateId) {}
    public record PickupProposalRequest(@NotNull Instant startTime,@NotNull Instant endTime,@NotNull AuthorizedPickupPerson authorizedPerson) {}
    public record ConfirmPickupRequest(boolean accepted) {}
    public record TemplateRequest(@NotNull ChatMessageTemplate.Type type,@NotBlank String title,@NotBlank String message) {}
}