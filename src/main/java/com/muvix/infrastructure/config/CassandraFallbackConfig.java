package com.muvix.infrastructure.config;

import com.muvix.application.port.out.persistence.CatalogProjectionPort;
import com.muvix.application.port.out.persistence.CatalogQueryPort;
import com.muvix.domain.model.catalogue.Batch;
import com.muvix.domain.model.catalogue.Product;
import com.muvix.infrastructure.adapters.out.persistence.cassandra.entity.CatalogItemCassandraEntity;
import com.muvix.infrastructure.adapters.out.persistence.cassandra.entity.PublicBoxCassandraEntity;
import com.muvix.infrastructure.adapters.out.persistence.cassandra.repository.CatalogItemCassandraRepository;
import com.muvix.infrastructure.adapters.out.persistence.cassandra.repository.PublicBoxCassandraRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Configuration
public class CassandraFallbackConfig {

    @Bean
    @ConditionalOnMissingBean(CatalogQueryPort.class)
    public CatalogQueryPort catalogQueryPortFallback() {
        return new CatalogQueryPort() {
            @Override
            public List<CatalogItemCassandraEntity> findByZone(String zone) {
                return List.of();
            }

            @Override
            public List<CatalogItemCassandraEntity> findByZoneWithLimit(String zone, int limit) {
                return List.of();
            }

            @Override
            public List<CatalogItemCassandraEntity> findByZoneAndCategory(String zone, String category) {
                return List.of();
            }

            @Override
            public List<PublicBoxCassandraEntity> findBoxesByZone(String zone) {
                return List.of();
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(CatalogProjectionPort.class)
    public CatalogProjectionPort catalogProjectionPortFallback() {
        return new CatalogProjectionPort() {
            @Override
            public void projectProductToCatalog(Product product, String zone, Long boxId) {
            }

            @Override
            public void projectBatchToCatalog(Batch batch, String zone) {
            }

            @Override
            public void updateDynamicPriceInCatalog(Long itemId, String zone, BigDecimal newPrice) {
            }

            @Override
            public void removeFromCatalog(Long itemId, String zone) {
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(CatalogItemCassandraRepository.class)
    public CatalogItemCassandraRepository catalogItemCassandraRepositoryFallback() {
        return createEmptyRepositoryProxy(CatalogItemCassandraRepository.class);
    }

    @Bean
    @ConditionalOnMissingBean(PublicBoxCassandraRepository.class)
    public PublicBoxCassandraRepository publicBoxCassandraRepositoryFallback() {
        return createEmptyRepositoryProxy(PublicBoxCassandraRepository.class);
    }

    @SuppressWarnings("unchecked")
    private <T> T createEmptyRepositoryProxy(Class<T> repoInterface) {
        return (T) Proxy.newProxyInstance(
                repoInterface.getClassLoader(),
                new Class<?>[]{repoInterface},
                (proxy, method, args) -> switch (method.getName()) {
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> args != null && args.length > 0 && proxy == args;
                    case "toString" -> repoInterface.getSimpleName() + "Fallback";
                    default -> {
                        if (method.getReturnType().equals(List.class)) yield List.of();
                        if (method.getReturnType().equals(Optional.class)) yield Optional.empty();
                        if (method.getReturnType().equals(boolean.class)) yield false;
                        if (method.getReturnType().equals(long.class)) yield 0L;
                        yield null;
                    }
                }
        );
    }
}
