package com.muvix.application.usecase.catalogue;

import com.muvix.application.port.in.catalogue.UploadImageUseCase;
import com.muvix.application.port.out.persistence.ImageStoragePort;
import com.muvix.domain.model.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class ImageStorageService implements UploadImageUseCase {

    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private final ImageStoragePort imageStoragePort;

    @Override
    public String upload(UploadImageUseCase.UploadCommand command) {
        if (command.content() == null || command.content().length == 0) {
            throw new DomainException("Image file cannot be empty");
        }
        if (command.contentType() == null || !ALLOWED_TYPES.contains(command.contentType())) {
            throw new DomainException("Only JPG, PNG or WEBP images are allowed");
        }
        return imageStoragePort.upload(command.content(), command.fileName(), command.contentType());
    }
}