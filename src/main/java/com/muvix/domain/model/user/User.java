package com.muvix.domain.model.user;

import com.muvix.domain.model.exception.DomainException;
import java.util.Objects;

/**
 * Single user entity. "Seller" and "Buyer" are contextual roles assumed depending on the interaction:
 *  - User is Seller of a RelocationEvent if event.userId == user.id
 *  - User is Buyer of an Order / Offer if order.buyerId == user.id
 */
public class User {

    private final Long id;
    private String name;
    private String email;
    private String phone;

    public User(Long id, String name, String email, String phone) {
        if (name == null || name.isBlank()) throw new DomainException("Name is required");
        if (email == null || email.isBlank()) throw new DomainException("Email is required");
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }

    public void updateContact(String phone) {
        this.phone = phone;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof User u && id != null && id.equals(u.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
