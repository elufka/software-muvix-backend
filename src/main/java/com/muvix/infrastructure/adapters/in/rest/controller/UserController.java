package com.muvix.infrastructure.adapters.in.rest.controller;

import com.muvix.application.port.in.user.RegisterUserUseCase;
import com.muvix.application.usecase.user.MobbexConnectService;
import com.muvix.infrastructure.adapters.in.rest.dto.request.CreateUserRequest;
import com.muvix.infrastructure.adapters.in.rest.dto.response.UserResponse;
import com.muvix.infrastructure.adapters.in.rest.mapper.UserRestMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final RegisterUserUseCase registerUserUseCase;
    private final MobbexConnectService mobbexConnectService;
    private final UserRestMapper userRestMapper;

    public UserController(RegisterUserUseCase registerUserUseCase,
                          MobbexConnectService mobbexConnectService,
                          UserRestMapper userRestMapper) {
        this.registerUserUseCase = registerUserUseCase;
        this.mobbexConnectService = mobbexConnectService;
        this.userRestMapper = userRestMapper;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterUserUseCase.RegistrationResult> registerUser(@Valid @RequestBody CreateUserRequest request) {
        RegisterUserUseCase.RegisterUserCommand command = new RegisterUserUseCase.RegisterUserCommand(
                request.name(), request.email(), request.phone(), request.password()
        );
        RegisterUserUseCase.RegistrationResult result = registerUserUseCase.registerUser(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping("/{id}/connect/mobbex-url")
    public ResponseEntity<Map<String, String>> getMobbexConnectUrl(@PathVariable Long id) {
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