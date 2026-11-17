# CRUD y ciclo de vida por agregado

Este inventario se basa en el código actual, no solo en nombres de clases. “Disponible” significa que existe un flujo REST o de aplicación usable; “parcial” indica que solo hay operaciones de dominio/persistencia. Las claves escalares (`user_id`, `event_id`, etc.) no son claves foráneas explícitas salvo las columnas de colección con `@CollectionTable`.

| Agregado / tabla | Crear | Leer | Reemplazar / actualizar | Eliminar | Persistencia y limitaciones |
|---|---|---|---|---|---|
| Usuario (`users`) | `POST /api/v1/users/register` | `GET /api/v1/users/me` | `PUT /api/v1/users/me`, `PATCH /me/contact`; lifecycle/2FA por endpoints | solicitud y confirmación de borrado/anonimización; no hay borrado físico arbitrario | Contraseña BCrypt; JWT + TOTP; inactividad anual desde último login. Solicitudes con transacciones activas quedan pendientes hasta que se completen y luego requieren confirmación. |
| Artículo/lote (`sellable_items`, `product_images`, `batch_products`) | `POST /api/v1/products`, `POST /api/v1/batches` | `GET /api/v1/items/{id}` (solo vendedor) | `PUT /api/v1/items/{id}`; reservado/vendido/borrado lógico no editable | `DELETE /api/v1/items/{id}` lógico. Borrar lote conserva productos; borrar producto lo separa de lotes disponibles y retira el lote si queda con menos de dos | `SellableItemAdapter`; herencia JPA `SINGLE_TABLE`. Reservar lote también reserva productos miembros. En pago aprobado se marcan vendidos. No existe endpoint público para listados del vendedor. |
| Evento/caja (`relocation_events` + colecciones) | `POST /api/v1/boxes/{sale,purchase,full-relocation}` | `GET /api/v1/boxes/{id}` para propietario/miembro | `PUT /api/v1/boxes/{id}`; solo dueño y eventos abiertos | `DELETE /api/v1/boxes/{id}` cancela el evento y conserva relaciones/historial | `RelocationEventMySQLAdapter`; actualización conserva items asociados. No hay borrado físico. Alta y actualizaciones requieren DTO completo de campos específicos. |
| Pedido (`orders`, `order_items`) | compra directa, Take It All, oferta aceptada, checkout de carrito | persistencia por ID; falta endpoint GET | estados de pago, slot y entrega; no hay reemplazo genérico | sin DELETE: usar estados | `OrderMysqlAdapter`. Respuesta de compra incluye `{order, checkoutUrl}`. La orden admite `event_id` nulo cuando es una compra independiente; Take It All guarda el evento de venta. Una orden solo puede tener un vendedor. |
| Oferta (`offers`) | `POST /api/v1/offers` | persistencia por ID; no hay endpoint GET | aceptación/rechazo por vendedor | sin DELETE del flujo | `OfferMySQLAdapter`; aceptación crea orden al precio ofrecido y abre checkout. No se observan expiración automática ni historial de contraofertas. |
| Turno de retiro (`pickup_slots`) | `POST /api/v1/pickups/slots/propose` | no hay endpoint GET | contrapropuesta de comprador o vendedor; cada cambio reinicia ambas confirmaciones; se confirma cuando ambos aceptan | sin borrado expuesto | `PickupSlotMySQLAdapter`; `Order.status` avanza una vez que ambos confirman. El vendedor se deriva del artículo de la orden. |
| Disputa (`disputes`, `dispute_evidence`) | `POST /api/v1/disputes` | búsqueda por id interna; no hay GET REST | evidencia adicional (`POST /{id}/evidence`) y decisión moderador | sin DELETE | Estados conservan evidencia e historial. Una sola ronda adicional, con plazo fijo de 2 días. Sin respuesta o rechazo final libera escrow al vendedor. Moderador usa rol DB `MODERATOR`. Notificación por email/teléfono queda fuera de la aplicación según decisión del usuario. |
| Escrow (`escrow_accounts`) | creado al reservar/checkout | consultar por pedido/estado según servicio | `HELD`, `DISPUTED`, refund o release; actualización desde webhook | sin DELETE | Acciones pasan por Mobbex. Auto-release ahora se basa en `delivered_at + 24h` y excluye estados disputados. El callback comprueba el estado con operación Mobbex autenticada por reference; verificar firma HMAC de cuenta y manejo de pagos rechazados antes de producción. |
| Modelo de catálogo Cassandra (`catalog_items`, cajas públicas) | proyección del read model | por zona/categoría | re-proyección mediante puerto | no CRUD general | Vistas derivadas, no fuente de verdad. No hay consistencia transaccional/outbox demostrada. |
| `TwoFactorAuth`, `AuditLog`, ubicaciones y eventos de dominio | valores/eventos en memoria; 2FA se materializa parcialmente en `users` | no son recursos CRUD REST autónomos | 2FA por comandos; `AuditLog` inmutable por dominio | no se borra por CRUD | No se encontró tabla/adapter de auditoría para `AuditLog`; secretos de autenticador y hashes de recovery viven junto al usuario. |

## Decisiones funcionales aplicadas

- Mantener productos independientes al cancelar o borrar un lote.
- Permitir la publicación individual de productos que pertenecen a lotes; reservar un lote hace no disponibles sus productos. La compra individual separa el producto del lote; un lote con menos de dos productos queda `SOFT_DELETED`.
- Usuarios: borrado lógico por inactividad luego de un año desde login exitoso. Solicitud de eliminación con transacciones abiertas deja la cuenta activa hasta completar operaciones; entonces se solicita confirmación. La limpieza retiene nombre e ID históricos y borra datos sensibles de acceso/contacto.
- Pedidos, escrow y disputas preservan historial mediante estados, no DELETE físico.
- Disputas: un plazo fijo de 2 días para evidencia adicional, no extensible.
- Compras de varios vendedores se dividen en órdenes separadas (cada orden solo lleva un vendedor); Take It All incluye los artículos disponibles del evento y usa su precio configurado.
- La ubicación en mapa interactivo continúa siendo una propuesta; no se añadió búsqueda de mapa.

## Vacíos conocidos que requieren una decisión o implementación posterior

- El carrito carece de persistencia: `findCartItemsByBuyerId` sigue lanzando `UnsupportedOperationException`; checkout del carrito no puede operar hasta diseñar y persistir la asociación comprador-artículos.
- Las compras reservan inventario al crear el checkout. El webhook aprobado finaliza el estado vendido; falta una regla explícita de expiración y liberación de reservas ante checkout abandonado o pago rechazado.
- Falta endpoint de consulta para órdenes, ofertas, disputas y pickup slots, aunque existan puertos/repositorios.
- Los puntos de entrada que derivan usuario del JWT se actualizaron para catálogo, cajas, órdenes, pickups, ofertas y disputas; revisar/normalizar el resto de controladores para que nunca confíen en IDs del body/query.
- La integración de webhook ya consulta la operación oficial, pero requiere prueba con payloads reales/sandbox, política de firmas HMAC según configuración de la cuenta e idempotencia integral antes de producción.
- `AuditLog` carece de almacenamiento append-only.
- La escritura al proveedor de pagos y la transacción SQL no comparten transacción distribuida; se necesita outbox/reconciliación para fallos entre ambas operaciones.
