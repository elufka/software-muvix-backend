package com.muvix.infrastructure.adapters.in.rest.controller;

import com.muvix.application.port.in.catalogue.UploadImageUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final UploadImageUseCase uploadImageUseCase;

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        String secureUrl = uploadImageUseCase.upload(
                new UploadImageUseCase.UploadCommand(
                        file.getBytes(),
                        file.getOriginalFilename(),
                        file.getContentType()
                )
        );
        return ResponseEntity.ok(Map.of("url", secureUrl));
    }
}
