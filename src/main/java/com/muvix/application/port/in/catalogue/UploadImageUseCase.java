package com.muvix.application.port.in.catalogue;

public interface UploadImageUseCase {
    record UploadCommand(
            byte[] content,
            String fileName,
            String contentType
    ) {}

    String upload(UploadCommand command);
}
