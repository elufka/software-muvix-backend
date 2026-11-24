package com.muvix.domain.model.user;

import com.muvix.domain.model.exception.DomainException;
import java.util.Objects;

/**
 * Single user entity. "Seller" and "Buyer" are contextual roles assumed depending on the interaction:
 *  - User is Seller of a RelocationEvent if event.userId == user.id
 *  - User is Buyer of an Order / Offer if order.buyerId == user.id
 */
public class User {

    public enum Role { USER, MODERATOR }

    private final Long id;
    private String photo;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String country;
    private String province;
    private String city;
    private String address;
    private Role role;

    public User(Long id, String photo, String firstName, String lastName, String email,
                String phone, String country, String province, String city, String address, Role role) {
        if (firstName == null || firstName.isBlank()) throw new DomainException("First name is required");
        if (lastName == null || lastName.isBlank()) throw new DomainException("Last name is required");
        if (email == null || email.isBlank()) throw new DomainException("Email is required");
        this.id = id;
        this.photo = photo;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.country = country;
        this.province = province;
        this.city = city;
        this.address = address;
        this.role = (role == null) ? Role.USER : role;
    }

    public Long getId() { return id; }
    public String getPhoto() { return photo; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getCountry() { return country; }
    public String getProvince() { return province; }
    public String getCity() { return city; }
    public String getAddress() { return address; }
    public Role getRole() { return role; }
    public void updateContact(String phone) {
        this.phone = phone;
    }

    public void updateProfile(String firstName, String lastName, String email, String phone, String country, String province, String city, String address) {
        if (firstName == null || firstName.isBlank()) throw new DomainException("First name is required");
        if (lastName == null || lastName.isBlank()) throw new DomainException("Last name is required");
        if (email == null || email.isBlank()) throw new DomainException("Email is required");
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.country = country;
        this.province = province;
        this.city = city;
        this.address = address;
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
