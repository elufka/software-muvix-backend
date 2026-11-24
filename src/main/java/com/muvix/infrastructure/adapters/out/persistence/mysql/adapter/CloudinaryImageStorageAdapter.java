package com.muvix.infrastructure.adapters.out.persistence.mysql.adapter;

import com.cloudinary.Cloudinary;
import com.muvix.application.port.out.persistence.ImageStoragePort;
import com.muvix.domain.model.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;


@Component
@RequiredArgsConstructor
public class CloudinaryImageStorageAdapter implements ImageStoragePort {

    private final Cloudinary cloudinary;

    @Override
    public String upload(byte[] content, String fileName, String contentType) {
        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    content,
                    Map.of("folder", "muvix/publications")
            );
            return result.get("secure_url").toString();
        } catch (IOException e) {
            throw new DomainException("Error uploading image to storage provider");
        }
    }

    @Override
    public void delete(String imageUrl) {

    }
}
