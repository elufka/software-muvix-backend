package com.muvix.infrastructure.adapters.out.persistence.mysql.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class AuthorizedPickupPersonEmbeddable {
    @Column(name = "authorized_person_name")
    private String fullName;
    @Column(name = "authorized_person_doc")
    private String documentId;
    @Column(name = "authorized_person_phone")
    private String phone;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
