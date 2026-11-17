# Auditoría de código y mejoras trazables

## Evaluación

El código sigue una intención hexagonal (`application.port.*`, servicios de casos de uso, `infrastructure.adapters.*`) con límites parciales. Antes de los cambios había servicios vacíos, puertos sin adaptadores y un perfil de desarrollo con credenciales Mobbex literales. La persistencia transaccional faltante dejaba sin ejecución varios flujos que ya estaban expuestos. Se preservaron reglas de negocio existentes; las decisiones faltantes se preguntaron al usuario antes de implementar los flujos de compra, baja de cuenta, pickup y disputas.

## Hallazgos y cambios

### 1. Paquete inválido en búsqueda de cercanía
- **Módulo / evidencia:** `application.usecase.catalogue.FindNearbyCatalogService` estaba bajo el directorio del paquete pero no declaraba `package`.
- **Problema:** no era importable por el nombre esperado y Spring no podía encontrarlo desde el escaneo normal.
- **Solución aplicada:** declaración de paquete agregada; el caso de uso mantiene su contrato geográfico existente.
- **Ventajas:** carga coherente y estructura de paquetes verificable.
- **Riesgo/costo:** mínimo. La consulta sigue siendo por zonas; no es búsqueda precisa por mapa.

### 2. Persistencia incompleta entre dominio y JPA
- **Módulos / evidencia:** `RelocationEvent`, `Order`, `Dispute`, `Offer` y `PickupSlot` tenían puertos/casos de uso sin mapeo completo o adapter.
- **Problema:** los flujos podían fallar al guardar o reconstituir objetos; detalles de pickup y relaciones de evento no sobrevivían a la persistencia.
- **Solución aplicada:** se agregaron entidades/repositorios/adaptadores y mapeadores para eventos, órdenes, disputas, ofertas y slots; se conserva `event_id` nullable para compras independientes.
- **Ventajas:** separación entre modelo y persistencia; estado de flujos recuperable.
- **Riesgos/costo:** las anotaciones describen esquema, pero no hay migraciones versionadas; validar el DDL antes de producción.

### 3. Registro sin autenticación completa ni lifecycle de credenciales
- **Módulos / evidencia:** `RegisterUserService`, `UserJpaEntity`, `SecurityConfig`.
- **Problema:** registro recibía contraseña sin guardado seguro y no existían endpoints de token/2FA.
- **Solución aplicada:** BCrypt, JWT de acceso corto, TOTP, recovery codes de un solo uso, rol en claim, verificación de estado de cuenta por token, perfiles de usuario y lifecycle con baja por inactividad y borrado solicitado/anónimo.
- **Ventajas:** credenciales protegidas, control de acceso por estado y soporte explícito para el requisito 2FA.
- **Riesgos/costo:** TOTP requiere reloj sincronizado y despliegue seguro de `JWT_SECRET`; la consulta de cuenta por token agrega lectura a cada petición. Moderator se asigna directamente en base.

### 4. Checkout sin reserva/pago y liberación de escrow basada en antigüedad incorrecta
- **Módulos / evidencia:** `PlaceOrderService`, `CartAndCheckoutService`, `EscrowAutoReleaseScheduler`, `ProcessMobbexWebhookService`.
- **Problema:** checkout no devolvía URL ni vinculaba operación externa, y la liberación automática consultaba antigüedad de funding en lugar de fecha de entrega.
- **Solución aplicada:** compra directa/Take It All/oferta crea una orden monovendedor, reserva inventario y devuelve `{order, checkoutUrl}`; Take It All usa precio configurado e ítems actualmente disponibles. Webhook aprobado marca ítems vendidos. El scheduler usa órdenes entregadas anteriores al umbral de 24h y excluye escrow `DISPUTED`.
- **Ventajas:** transición mejor alineada con el ciclo compra→entrega→liberación.
- **Riesgos/costo:** Mobbex y SQL no comparten transacción; hacen falta idempotencia, autenticación del webhook y reconciliación/outbox. Aún no existe expiración automática de reservas ni restauración por pago rechazado.

### 5. Inventario de lote incompatible con venta individual confirmada
- **Módulos / evidencia:** `Batch`, `SellableItem`, `PlaceOrderService`, `ManageSellableItemService`.
- **Problema:** productos integrantes y lotes no tenían ciclo de disponibilidad coordinado y el borrado separado no protegía composición mínima.
- **Solución aplicada:** artículos y lotes reciben CRUD de vendedor; borrado lógico, separación de productos al borrar individualmente y baja lógica del lote si queda con menos de dos. Comprar lote reserva/venta también bloquea sus productos miembros.
- **Ventajas:** respeta independencia de productos y evita vender simultáneamente hijo y lote.
- **Riesgos/costo:** se mantiene asociación por IDs escalares; considerar restricciones/locking y pruebas de concurrencia para reservas paralelas.

### 6. Pickup carecía de persistencia y acuerdo bilateral
- **Módulos / evidencia:** `SchedulePickupSlotService`, `PickupSlot`, `PickupController`.
- **Problema:** un solo `confirm()` cerraba el turno y las propuestas no se guardaban.
- **Solución aplicada:** tabla/adapter de slots; ambas partes deben confirmar la propuesta vigente; una contrapropuesta de cualquiera reinicia las confirmaciones. Los endpoints derivan el actor del JWT.
- **Ventajas:** acuerdo bilateral y estado persistido.
- **Riesgos/costo:** no hay GET/listado/historial de propuestas anterior; los cambios reemplazan la propuesta actual.

### 7. Disputa no administraba evidencia ni coordinaba resultado financiero
- **Módulos / evidencia:** `OpenDisputeService`, `ManageDisputeService`, `DisputeController`.
- **Problema:** solo existía apertura, sin flujo para insuficiencia de evidencia ni settlement del escrow.
- **Solución aplicada:** el rol `MODERATOR` puede pedir evidencia adicional o resolver; el comprador tiene una ronda adicional de 2 días no extensible. Vencimiento o rechazo final libera fondos al vendedor; refund aprobado ejecuta refund Mobbex.
- **Ventajas:** estado auditable y regla temporal definida por el usuario.
- **Riesgos/costo:** email/teléfono a cargo del moderador queda fuera del sistema; llamadas Mobbex requieren idempotencia y reconciliación ante fallos transaccionales.

### 8. Identidad del actor recibida por body/query
- **Módulos / evidencia:** `OrderController`, `PickupController`, `DisputeController`, `BoxController`, `OfferController`, `SellableItemController`.
- **Problema:** el cliente podía enviar identificadores de comprador/vendedor ajenos al token.
- **Solución aplicada:** los nuevos flujos de orden, catálogo, cajas, ofertas, disputas y pickup derivan actor del subject JWT. Revisar controladores heredados restantes, especialmente cualquier endpoint que acepte `userId` en parámetros.
- **Ventajas:** reduce suplantación horizontal en operaciones nuevas.
- **Riesgos/costo:** cambios de contrato para clientes que enviaban IDs manualmente; la verificación integral de todos los endpoints sigue pendiente.

## Patrones y principios

- **Adapter/Repository:** puertos para persistence de negocio y adaptadores JPA/Cassandra/Mobbex.
- **Factory:** `EscrowGatewayFactory` selecciona implementación financiera por proveedor.
- **State:** las transiciones de orden, escrow, disputa, artículo y cuenta expresan lifecycle con estados.
- **Domain events:** se conservan eventos para disputa/aceptación y publicación Spring.
- **SOLID/GRASP:** DIP en gateways y repositorios; Information Expert en validación de dominio de `SellableItem`, `Batch`, `Order` y `Dispute`; Controller como coordinador HTTP; baja acoplación al evitar IDs de actor enviados por cliente en los endpoints nuevos.

## Deuda y límites

- El carrito ya se persiste por comprador y permite agregar/quitar/ver artículos; falta definir cómo actuar ante artículos de varios vendedores (rechazo actual), y cómo vencer o liberar reservas si el checkout no concluye.
- El callback Mobbex verifica el estado consultando la operación por reference con credenciales server-to-server y evita repetir la transición vendida; el proveedor documenta webhooks inbound, consulta de operación por referencia e idempotencia necesaria ([referencia oficial Mobbex](https://www.mobbex.com/desarrolladores/postman/)). No se implementó verificación HMAC porque la colección oficial indica que se usa si está configurada; confirmar configuración real de la cuenta y reforzar idempotencia/rechazo de pagos antes de producción.
- Operaciones externas de checkout/refund/capture y transacciones SQL no son atómicas; aplicar outbox/saga/reconciliación.
- Faltan endpoints de listado general para pedidos, ofertas, disputas, slots y artículos; las consultas por ID de órdenes, ofertas, disputas, escrow y slots validan participantes.
- No hay almacenamiento append-only para `AuditLog`; no hay migraciones versionadas para nuevas tablas/columnas.
- El mapa interactivo y la búsqueda geoespacial no se implementaron. La cercanía actual agrupa por zonas y el adapter de zona cercana no calcula vecinos ni distancia exacta.
- Requiere validar el esquema real de MySQL/PostgreSQL y las URLs/credenciales del entorno: el POM, nombres de paquetes y perfiles no acreditan por sí solos la base desplegada.

## Verificación

- `mvn -q -DskipTests compile`: completó correctamente en el árbol actualizado.
- `mvn -q test`: completó correctamente después de añadir las implementaciones recientes y ajustar los fakes de puertos del test; el contexto Spring arrancó con perfil de test y H2.
- El contexto Spring arrancó en unos 12 segundos en la última ejecución. El log mostró advertencias de escaneo de repositorios multi-store y `open-in-view`. No se verificó una instancia de producción, base real ni credenciales/proveedor externo.
