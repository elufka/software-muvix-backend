package com.muvix.infrastructure.adapters.in.rest.controller;

import com.muvix.application.port.in.user.RegisterUserUseCase;
import com.muvix.application.port.in.user.ManageUserProfileUseCase;
import com.muvix.application.port.in.user.ManageUserAccountLifecycleUseCase;
import com.muvix.application.usecase.user.MobbexConnectService;
import com.muvix.domain.model.user.User;
import com.muvix.infrastructure.adapters.in.rest.dto.request.CreateUserRequest;
import com.muvix.infrastructure.adapters.in.rest.dto.request.ReplaceUserProfileRequest;
import com.muvix.infrastructure.adapters.in.rest.dto.request.UpdateUserRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final RegisterUserUseCase registerUserUseCase;
    private final ManageUserProfileUseCase manageUserProfileUseCase;
    private final ManageUserAccountLifecycleUseCase accountLifecycleUseCase;
    private final MobbexConnectService mobbexConnectService;

    public UserController(RegisterUserUseCase registerUserUseCase,
                          ManageUserProfileUseCase manageUserProfileUseCase,
                          ManageUserAccountLifecycleUseCase accountLifecycleUseCase,
                          MobbexConnectService mobbexConnectService) {
        this.registerUserUseCase = registerUserUseCase;
        this.manageUserProfileUseCase = manageUserProfileUseCase;
        this.accountLifecycleUseCase = accountLifecycleUseCase;
        this.mobbexConnectService = mobbexConnectService;
    }

    @GetMapping("/me/data-erasure")
    public ResponseEntity<ManageUserAccountLifecycleUseCase.AccountLifecycleStatus> getDataErasureStatus(
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(accountLifecycleUseCase.getErasureStatus(Long.valueOf(jwt.getSubject())));
    }

    @PostMapping("/me/data-erasure")
    public ResponseEntity<ManageUserAccountLifecycleUseCase.AccountLifecycleStatus> requestDataErasure(
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.accepted().body(accountLifecycleUseCase.requestDataErasure(
                Long.valueOf(jwt.getSubject())));
    }

    @PostMapping("/me/data-erasure/confirm")
    public ResponseEntity<ManageUserAccountLifecycleUseCase.AccountLifecycleStatus> confirmDataErasure(
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(accountLifecycleUseCase.confirmDataErasure(Long.valueOf(jwt.getSubject())));
    }

    @GetMapping("/me")
    public ResponseEntity<User> getMyProfile(@AuthenticationPrincipal Jwt jwt) {
        var user = manageUserProfileUseCase.getProfile(Long.valueOf(jwt.getSubject()));
        return ResponseEntity.ok(user);
    }
/*
    @PutMapping("/me")
    public ResponseEntity<User> replaceMyProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ReplaceUserProfileRequest request) {
        var user = manageUserProfileUseCase.replaceProfile(
                new ManageUserProfileUseCase.UpdateProfileCommand(
                        Long.valueOf(jwt.getSubject()), request.name(), request.email(), request.phone()));
        return ResponseEntity.ok(user);
    }

    @PatchMapping("/me/contact")
    public ResponseEntity<User> updateMyContact(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateUserRequest request) {
        var current = manageUserProfileUseCase.getProfile(Long.valueOf(jwt.getSubject()));
        var updated = manageUserProfileUseCase.replaceProfile(
                new ManageUserProfileUseCase.UpdateProfileCommand(
                        current.getId(), current.getName(), current.getEmail(), request.phone()));
        return ResponseEntity.ok(updated);
    }
*/
    @PostMapping("/{id}/connect/mobbex-url")
    public ResponseEntity<Map<String, String>> getMobbexConnectUrl(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        if (!id.equals(Long.valueOf(jwt.getSubject()))) {
            return ResponseEntity.status(403).build();
        }
        String url = mobbexConnectService.generateConnectUrl(id);
        return ResponseEntity.ok(Map.of("connectUrl", url));
    }

    @GetMapping(value = "/connect/callback", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String handleMobbexCallback(@RequestParam("reference") Long sellerId,
                                       @RequestParam("status") String status,
                                       @RequestParam(value = "id", required = false) String connectId) {
        boolean success = mobbexConnectService.processConnectCallback(sellerId, status, connectId);
        return """
            <!DOCTYPE html>
            <html>
            <body>
                <script>
                    window.parent.postMessage({ type: 'MOBBEX_CONNECT_RESPONSE', success: %b, sellerId: %d }, '*');
                </script>
            </body>
            </html>
            """.formatted(success, sellerId);
    }
}
