package com.muvix.application.port.out.persistence;

import com.muvix.domain.model.user.User;
import java.util.Optional;

/**
 * Puerto de Salida (Output Port) para el manejo y persistencia de Usuarios.
 * Permite que los Casos de Uso interactúen únicamente con la entidad de Dominio 'User',
 * aislando completamente la aplicación de Spring Data JPA, MySQL o cualquier otro motor.
 */
public interface UserRepositoryPort {

    /**
     * Guarda o actualiza un usuario en el sistema.
     */
    User save(User user);

    /**
     * Busca un usuario por su ID único.
     */
    Optional<User> findById(Long id);

    /**
     * Busca un usuario por su dirección de correo electrónico.
     */
    Optional<User> findByEmail(String email);

    /**
     * Vincula o desvincula la cuenta de Mobbex Connect de un usuario.
     */
    void updateMobbexConnectDetails(Long userId, String mobbexConnectId, boolean linked);
}
