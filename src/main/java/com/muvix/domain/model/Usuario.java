package com.muvix.domain.model;

import java.util.Objects;

/**
 * Un único tipo de usuario. "Vendedor" y "comprador" NO son subtipos: son roles
 * que el usuario asume según el contexto:
 *  - es vendedor de un EventoMudanza si evento.vendedorId == usuario.id
 *  - es comprador de un Pedido / Oferta si pedido.compradorId == usuario.id
 * Así el mismo usuario puede vender su mudanza y comprar la de otro sin cambiar de tipo.
 */
public class Usuario {

    private final Long id;
    private String nombre;
    private String email;
    private String telefono;

    public Usuario(Long id, String nombre, String email, String telefono) {
        if (nombre == null || nombre.isBlank()) throw new DominioException("El nombre es obligatorio");
        if (email == null || email.isBlank()) throw new DominioException("El email es obligatorio");
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public String getTelefono() { return telefono; }

    public void actualizarContacto(String telefono) {
        this.telefono = telefono;
    }

    @Override public boolean equals(Object o) {
        return o instanceof Usuario u && id != null && id.equals(u.id);
    }
    @Override public int hashCode() { return Objects.hashCode(id); }
}
