# Diagramas de arquitectura

Los diagramas reflejan el código entregado y marcan como propuesta cualquier componente futuro.

## Componentes actuales

```mermaid
flowchart LR
  REST[REST controllers] --> In[Puertos de entrada]
  In --> UC[Servicios de aplicación]
  UC --> Domain[Dominio: catálogo, eventos, logística, transacciones, usuarios]
  UC --> Query[CatalogQueryPort]
  Query --> CAdapter[CatalogCassandraAdapter]
  CAdapter --> CRepo[Repositorios Cassandra]
  UC --> ItemPort[SellableItemRepositoryPort]
  ItemPort --> JPA[Adaptadores/repositorios JPA]
  UC --> Proj[CatalogProjectionPort]
  Proj -. implementación no incluida .-> Cass[(Cassandra read model)]
  UC --> Pay[Puertos de pago]
  Pay --> Mobbex[MobbexHttpAdapter]
  REST --> Location[LocationGatewayPort]
  UC --> Location
  Location --> OSM[OpenStreetMapHttpAdapter]
  UC --> EventPort[EventPublisherPort]
  EventPort --> SpringEvents[SpringEventPublisherAdapter]
```

Nota: el acceso directo de `SearchCatalogService` a repositorios Cassandra y las entidades Cassandra expuestas por contratos de aplicación son desviaciones del límite esperado, no flechas de una arquitectura ideal.

## Interacciones principales observadas

```mermaid
sequenceDiagram
  actor Cliente
  participant API as REST Controller
  participant Caso as Servicio de aplicación
  participant Dominio
  participant Puerto as Puerto de salida
  participant DB as Adaptador/persistencia
  Cliente->>API: alta/búsqueda/operación
  API->>Caso: comando o consulta
  Caso->>Dominio: crear/aplicar regla (cuando corresponde)
  Caso->>Puerto: persistir, consultar o integrar
  Puerto->>DB: llamada Spring Data / HTTP
  DB-->>Puerto: entidad/respuesta
  Puerto-->>Caso: resultado
  Caso-->>API: resultado del caso de uso
  API-->>Cliente: respuesta HTTP
```

Para cercanía en particular, el caso `FindNearbyCatalogService` resuelve zona mediante `LocationGatewayPort`, obtiene una lista de zonas y solicita artículos/cajas por zona a `CatalogQueryPort`. El adaptador incluido de OpenStreetMap responde hoy solo con la zona central ante consulta de zonas cercanas.

## Relaciones principales de dominio

```mermaid
classDiagram
  class SellableItem {
    <<abstract sealed>>
    Long id
    Long sellerId
    BigDecimal price
    State state
    isPublic()
    canBePurchased()
    reserve()
    markAsSold()
  }
  class Product {
    String category
    List~String~ images
  }
  class Batch {
    Set~Long~ productIds
    addProduct(Product)
    removeProduct(Long)
  }
  SellableItem <|-- Product
  SellableItem <|-- Batch
  class Order {
    Long buyerId
    Set~Long~ itemIds
    BigDecimal total
    Status status
  }
  class EscrowAccount
  class RelocationEvent
  class SaleEvent
  class PurchaseEvent
  RelocationEvent <|-- SaleEvent
  RelocationEvent <|-- PurchaseEvent
  Order --> RelocationEvent : asociado a evento
  EscrowAccount --> Order : custodia
```

El diagrama resume relaciones observables y no implica que las entidades JPA tengan el mismo grafo: el proyecto separa modelos de dominio y entidades de infraestructura.

## Arquitectura propuesta para búsqueda en mapa (no implementada)

```mermaid
flowchart LR
  UI[Cliente con mapa] --> API[MapSearchController]
  UI --> Tiles[Proveedor tiles/mapa]
  API --> UC[SearchCatalogInAreaUseCase]
  UC --> Area[GeoSearchArea / GeoPoint]
  UC --> Port[CatalogGeoSearchPort]
  Port --> Adapter[Adaptador de consulta geoespacial]
  Adapter --> Index[(Índice geoespacial / vista)]
  Adapter --> Model[CatalogMapItem neutral]
  Model --> UC
  UC --> API
  Write[Alta/cambio de ubicación] --> Outbox[Proyección confiable e idempotente]
  Outbox --> Index
  Address[Dirección opcional] --> GeoPort[LocationGatewayPort]
  GeoPort --> GeoAdapter[Geocoding adapter]
  GeoAdapter --> Provider[Nominatim u otro proveedor]
```

El proveedor de tiles sirve la capa visual; el backend consulta publicaciones propias. La búsqueda propuesta no se debe confundir con la resolución de zonas ya presente.
