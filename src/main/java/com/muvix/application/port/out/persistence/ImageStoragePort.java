package com.muvix.application.port.out.persistence;

public interface ImageStoragePort {
    String upload(byte[] content, String fileName, String contentType);
    void delete(String imageUrl);
}
