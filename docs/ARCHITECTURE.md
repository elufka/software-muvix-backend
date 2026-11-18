# Arquitectura de Muvix

## Propósito y dominio

Muvix combina un marketplace de productos/lotes de segunda mano con gestión de cajas/eventos de mudanza, coordinación de retiros y custodia de pagos. Los conceptos centrales observables son `SellableItem` (`Product` y `Batch`), eventos de venta/compra/mudanza, `Order`, `EscrowAccount`, usuarios y zonas de ubicación. El precio cero representa un artículo privado; las reglas de disponibilidad y reserva viven en `SellableItem`.

## Stack y build

- Java 21; Maven; Spring Boot 4.1.2 (según el `pom.xml` recibido).
- Spring MVC, Validation, Security, Spring Data JPA, Spring Data Cassandra.
- PostgreSQL como driver/runtime declarado; Cassandra para lecturas de catálogo.
- MapStruct y Lombok declarados. No se observa configuración de base ni migraciones en los archivos entregados.
- Integraciones HTTP con Mobbex y OpenStreetMap/Nominatim, además de eventos Spring.

El paquete entregado incluye `src/`, `pom.xml` y únicamente `maven-wrapper.properties`; no incluye wrapper ejecutable, scripts, README, archivos de migración ni infraestructura de despliegue.

## Estilo arquitectónico

La intención es Ports and Adapters (hexagonal): `application.port.in` define entradas de casos de uso, `application.port.out` define dependencias externas y `infrastructure.adapters` implementa controladores REST, persistencia e integraciones. El dominio está en `domain.model`. Esto se cumple parcialmente: por ejemplo, `BrowseCatalogService` depende de un puerto pero devuelve una entidad Cassandra; `CatalogQueryPort` también expone entidades de infraestructura, y `SearchCatalogService` accede directamente a repositorios Spring Data. Hay filtrado y ordenamiento duplicados entre `FilterCatalogService` y `SearchCatalogService`.

## Organización y responsabilidades

| Área | Responsabilidad observada |
|---|---|
| `domain.model.catalogue` | Artículos, lotes, estados y reglas de disponibilidad/venta. |
| `domain.model.transaction` | Pedido, disputa y custodia. |
| `domain.model.management` | Eventos de compra, venta, mudanza y precios. |
| `application.port.in` | Contratos de casos de uso y comandos/consultas. |
| `application.usecase` | Coordinación de dominio, puertos e integraciones. |
| `application.port.out` | Persistencia, pagos, ubicación, seguridad y publicación de eventos. |
| `infrastructure.adapters.in.rest` | API HTTP, DTOs, mapeadores y manejo de excepciones. |
| `infrastructure.adapters.out.persistence.mysql` | Repositorios/adaptadores y mapeo JPA. |
| `infrastructure.adapters.out.persistence.cassandra` | Vistas de lectura por zona y repositorios Cassandra. |
| `infrastructure.adapters.out.payment`, `location`, `event` | Adaptadores Mobbex, Nominatim y eventos Spring. |
| `infrastructure.config`, `scheduler` | Configuración Spring y liberación programada de escrow. |

## Componentes principales y flujo

```mermaid
flowchart LR
  Client[Cliente REST] --> Controller[Controladores REST]
  Controller --> InPort[Puertos de entrada / casos de uso]
  InPort --> UseCase[Servicios de aplicación]
  UseCase --> Domain[Modelos y reglas de dominio]
  UseCase --> OutPort[Puertos de salida]
  OutPort --> Mysql[Adaptadores JPA / PostgreSQL]
  OutPort --> Cassandra[Adaptadores Cassandra / vistas de catálogo]
  OutPort --> Mobbex[Adaptador HTTP Mobbex]
  OutPort --> OSM[Adaptador HTTP Nominatim]
  OutPort --> Events[Publicador de eventos Spring]
```

El recorrido del feed es `BrowseCatalogUseCase` → `BrowseCatalogService` → `CatalogQueryPort` → `CatalogCassandraAdapter` → repositorio Cassandra. El alta pública es `CreateProductService` → dominio → puerto de artículos → `CatalogProjectionCassandraAdapter`. El flujo de cercanía está en `FindNearbyCatalogService`: geocodifica coordenadas, obtiene zonas, consulta artículos/cajas por zona y construye la respuesta. La ubicación concreta se llama a través de `LocationGatewayPort`.

Los flujos transaccionales añaden órdenes, escrows y pago Mobbex; cada checkout directo, Take It All u oferta aceptada compra una selección de un vendedor y devuelve `CheckoutResult` (no hay carrito). La programación de retiro persiste propuestas y espera confirmación explícita de ambas partes. Disputas guardan evidencia adicional, con plazo de dos días y acción de moderador. La compra de lote reserva los productos miembros. El comprador puede cancelar antes de que el vendedor confirme pickup; con fondos retenidos se solicita refund y se restaura inventario y pertenencias originales de lotes.

## Persistencia e integraciones

JPA almacena entidades de negocio transaccionales (paquetes/adaptadores se llaman MySQL, mientras que el POM/perfiles deben revisarse para identificar el motor de despliegue) y Cassandra ofrece consultas de catálogo particionadas por `zone`. Existe `CatalogProjectionCassandraAdapter`, pero la consistencia entre bases no es atómica y no hay outbox verificado. `OpenStreetMapHttpAdapter` implementa geocodificación/resolución de zona; `findNearbyZones` no ofrece cálculo real de vecindad geográfica. Mobbex se abstrae mediante puertos de pago y hay una factoría de gateway.

El usuario se autentica mediante contraseña BCrypt, 2FA TOTP y JWT con expiración corta. La configuración JWT exige secreto de al menos 32 bytes a través de `JWT_SECRET`. Los endpoints nuevos derivan el actor del subject JWT. El callback entrante Mobbex usa endpoint público y valida la operación mediante consulta autenticada por reference; la documentación oficial muestra el GET de operación por reference y describe HMAC cuando está configurado ([Mobbex](https://www.mobbex.com/desarrolladores/postman/)). Confirmar firma de la cuenta, payload real y reintentos/idempotencia antes de producción.

## Patrones observados

- Ports and Adapters parcial: contratos de entrada/salida y adaptadores.
- Repository: puertos de persistencia más Spring Data.
- Adapter: Mobbex, OpenStreetMap y publicador Spring.
- Factory: `EscrowGatewayFactory`.
- Domain Events: eventos del dominio y publicador Spring.
- CQRS/proyección: separación de escritura relacional y lectura Cassandra; falta consistencia garantizada/outbox.

Para el diagrama de relaciones y flujos complementarios, ver [ARCHITECTURE_DIAGRAMS.md](ARCHITECTURE_DIAGRAMS.md).
