# Auditoría y propuestas de mejora

## Alcance y evaluación inicial

Se inspeccionó el código fuente realmente incluido (Java, configuración, POM y el test de contexto). El diseño pretende usar arquitectura hexagonal, con un dominio de marketplace/mudanzas y adaptadores de base de datos, pagos, geolocalización y eventos. La separación no es uniforme: hay entidades Cassandra filtrándose a contratos/servicios de aplicación, acceso directo desde un servicio a repositorios Cassandra y duplicación de consultas de catálogo. El material tiene vacíos que impiden afirmar que el producto completo compile o ejecute: faltan varios adaptadores requeridos, implementaciones de servicios de casos de uso y configuraciones de persistencia.

Se encontró `FindNearbyCatalogService.java` sin declaración de paquete aunque está bajo `com/muvix/application/usecase/catalogue`; esto impide resolver la clase por su nombre/import esperado. Se corrigió agregando la declaración correcta. No se alteraron reglas de negocio.

## Mejoras trazables

### 1. Declaración de paquete ausente
- **Módulo:** `application.usecase.catalogue`.
- **Dominio:** el caso de uso consulta catálogo alrededor de una ubicación para usuarios que se mueven o buscan artículos próximos.
- **Problema original:** `FindNearbyCatalogService.java` no declaraba paquete y no correspondía a su ruta ni al resto de servicios.
- **Solución / implementación:** se agregó `package com.muvix.application.usecase.catalogue;` al inicio del archivo. **Implementada**.
- **Ventajas:** clase ubicable por Spring y consistente con convención y arquitectura.
- **Costos/riesgos:** cambio mínimo; debe verificarse con una compilación completa cuando Maven pueda resolver dependencias.

### 2. Contratos de catálogo acoplados a Cassandra
- **Módulo:** `application.port.out.persistence.CatalogQueryPort`, `BrowseCatalogUseCase`, `FilterCatalogUseCase`; entidades bajo `infrastructure.adapters.out.persistence.cassandra.entity`.
- **Dominio:** el catálogo expone publicaciones/cajas y el feed/filtros son lecturas públicas.
- **Problema actual:** los contratos de aplicación usan `CatalogItemCassandraEntity` y `PublicBoxCassandraEntity`. `BrowseCatalogService` retorna directamente una entidad Cassandra. La aplicación conoce el formato de persistencia y reemplazar Cassandra afecta casos de uso y API.
- **Solución propuesta:** definir modelos de lectura neutrales al storage en el puerto/capa de aplicación y mapearlos dentro de `CatalogCassandraAdapter`. Separar además DTO HTTP de modelos de lectura si la API los expone.
- **Implementación:** propuesta, no aplicada; requiere revisar todos los callers y contratos REST ausentes en el paquete.
- **Ventajas:** DIP, menor acoplamiento y persistencia reemplazable.
- **Desventajas:** mapeo y migración de tipos, posible esfuerzo de compatibilidad de serialización.

### 3. Dos caminos de filtrado de catálogo
- **Módulo:** `SearchCatalogService`, `FilterCatalogService`, `CatalogQueryPort`.
- **Dominio:** búsqueda por zona, categoría, precio y orden de publicaciones.
- **Problema actual:** `SearchCatalogService` repite filtro y orden que `FilterCatalogService` implementa, mientras el primero accede directamente a repositorios Spring Data en vez de usar el puerto. Las opciones de orden también son inconsistentes (enum en uno, strings en otro).
- **Solución propuesta:** elegir un solo caso de uso/contrato de filtro, centralizar política de filtros en aplicación y exponer una interfaz explícita de consulta. Tipar `sortBy`; decidir con evidencia de volumen si filtros/rangos se ejecutan en Cassandra o en servicio.
- **Implementación:** propuesta; mantener ambos contratos hasta encontrar controllers/callers completos evita romper funciones no incluidas.
- **Ventajas:** alta cohesión, menos divergencia de resultados y pruebas unitarias más simples.
- **Desventajas:** migración de llamadas y modelado de consultas; el filtrado en memoria puede ser costoso.

### 4. `findNearbyZones` no cumple su contrato de radio
- **Módulo:** puerto `LocationGatewayPort`, adaptador `OpenStreetMapHttpAdapter`, `FindNearbyCatalogService`.
- **Dominio:** búsqueda de servicios/artículos en zonas próximas y origen/destino de mudanza.
- **Problema actual:** el adaptador ignora `radiusKm` y devuelve solo el centro; el caso de uso consulta por zonas y no filtra por distancia. La respuesta puede aparentar una búsqueda radial que no se realiza.
- **Solución propuesta:** definir semántica (radio geográfico vs. vecindad administrativa), validación/rango y fuente de polígonos/zona; implementar un proveedor real de vecindad o limitar/renombrar el contrato hasta contar con esa regla. Añadir pruebas de límites y fallbacks.
- **Implementación:** propuesta, no se cambió el comportamiento geográfico existente.
- **Ventajas:** contrato honesto y resultados consistentes.
- **Desventajas:** requiere datos geoespaciales, decisiones de negocio y costo de consultas/operación.

### 5. Proyección de escritura a Cassandra sin atomicidad clara
- **Módulo:** `CreateProductService`, `CreateBatchService`, `CatalogProjectionPort`; repositorios MySQL/Cassandra.
- **Dominio:** artículos publicables y lotes deben aparecer en el catálogo de lectura luego del alta.
- **Problema actual:** la escritura relacional y la proyección se invocan en un mismo servicio transaccional, pero una transacción MySQL no hace atómica la escritura en Cassandra. No se ve implementación de `CatalogProjectionPort` en los archivos provistos. Un fallo parcial puede dejar la vista desactualizada.
- **Solución propuesta:** confirmar adaptador e infraestructura reales; para consistencia eventual, registrar outbox en transacción relacional y proyectar con consumidor idempotente/reintentos. Si requisito de consistencia fuerte, evaluar alternativa con costos explícitos.
- **Implementación:** propuesta; el adaptador/DDL ausentes impiden una modificación segura.
- **Ventajas:** recuperación y trazabilidad de eventos de proyección.
- **Desventajas:** más componentes, operación y eventual consistencia visible al usuario.

### 6. Responsabilidad de pagos en checkout
- **Módulo:** `CartAndCheckoutService`.
- **Dominio:** confirmar pedido e iniciar custodia de fondos.
- **Problema actual:** el servicio coordina carga del evento, carrito, total, creación de orden y gateway de escrow/pago en un único método. La llamada externa dentro del límite transaccional puede alargar locks y no es atómica con DB.
- **Solución propuesta:** mantener el caso de uso como coordinador, extraer cálculo/validación de checkout a dominio o política testeable y desacoplar llamada externa del commit con estado pendiente/outbox o saga, de acuerdo con garantías del proveedor.
- **Implementación:** propuesta; no se reescribió checkout porque eso podría modificar reglas financieras sin tests/contratos completos.
- **Ventajas:** cohesión, testabilidad y mejor manejo de fallos distribuidos.
- **Desventajas:** más estados y reconciliación; requiere conservar idempotencia de pagos y compatibilidad con webhooks.

## Patrones y principios

Ya hay puertos/adaptadores, repositorios, factoría de gateway y eventos. Las propuestas aplican DIP/OCP y GRASP Low Coupling al aislar Cassandra y proveedores; Information Expert al mantener reglas de artículos/precios dentro del dominio; Pure Fabrication para outbox/adaptadores cuando se justifiquen. No se agrega Strategy por sí sola: distintos proveedores de mapas serían intercambiables detrás de un puerto, solo cuando exista requerimiento concreto.

## Validación ejecutada

Se intentó `mvn test -q`. La ejecución se detuvo antes de compilar código: Maven no pudo descargar dependencias del plugin (`maven-filtering`, `plexus-build-api`, entre otras) porque el entorno no resolvió `repo.maven.apache.org` (`Unknown host`). No hay resultados de tests ni verificación de arranque. El único test provisto es `MuvixApplicationTests.contextLoads`; no cubre reglas de negocio. No se añadieron tests porque no fue posible resolver dependencias y el paquete recibido no ofrece un baseline ejecutable.

## Deuda y límites

El POM declara PostgreSQL mientras nombres/configuración se refieren a MySQL; hay archivos de configuración vacíos (`MysqlConfig`, `Cassandraconfig`, `SecurityConfig`). Aparecen clases duplicadas para entidad Cassandra y adaptadores de catálogo; un adaptador de consulta sin interfaz parece redundante. El árbol contiene servicios sin implementación/estereotipo y adaptadores requeridos ausentes. Hace falta el repositorio completo para resolver estas cuestiones con trazabilidad y verificar preservación integral de funcionalidades.
