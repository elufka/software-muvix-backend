package com.muvix.domain.model;

public class Item {
    private Long id;
    private String imgUrl;
    private Publication publication;


    // Constructor vacío
    public Item() {
    }

    // Constructor con campos
    public Item(Long id, String imgUrl, Publication publication) {
        this.id = id;
        this.imgUrl = imgUrl;
        this.publication = publication;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", imgUrl='" + imgUrl + '\'' +
                '}';
    }


}
