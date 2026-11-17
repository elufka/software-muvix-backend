package com.muvix.infrastructure.adapters.out.persistence.mysql.mapper;

import com.muvix.domain.model.user.User;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.UserJpaEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserPersistenceMapperTest {

    @Test
    void profileUpdatePreservesIntegrationAndSecurityFields() {
        UserJpaEntity entity = new UserJpaEntity();
        entity.setMobbexConnectId("connect-id");
        entity.setMobbexLinked(true);
        entity.setTwoFactorSecret("secret-value");
        entity.setTwoFactorEnabled(true);

        UserPersistenceMapper.updateEntity(
                new User(7L, "Updated Name", "updated@example.test", "555-0100"),
                entity);

        assertEquals("Updated Name", entity.getName());
        assertEquals("connect-id", entity.getMobbexConnectId());
        assertEquals(true, entity.isMobbexLinked());
        assertEquals("secret-value", entity.getTwoFactorSecret());
        assertEquals(true, entity.isTwoFactorEnabled());
    }
}
