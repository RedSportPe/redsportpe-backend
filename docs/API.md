# Contrato de integración

Base: `/api`. Salvo los endpoints públicos, enviar `Authorization: Bearer <token>`. Los importes están en PEN y se representan con `BigDecimal` en servidor. Los errores de negocio devuelven `application/problem+json`: 400 para solicitudes inválidas, 401 sin sesión válida, 403 por rol, 404 para recursos inexistentes o ajenos y 409 para conflictos de stock, versión, pago o estado.

## Identidad y tiendas

| Método | Ruta | Acceso / finalidad |
| --- | --- | --- |
| POST | `/auth/sign-up` | Público; `{name,email,password}`. Crea siempre un cliente. Después iniciar sesión. |
| POST | `/auth/sign-in` | Público; `{email,password}`. Devuelve usuario, puntos, proveedor, fecha, `storeCode` y `token`. |
| POST | `/auth/google` | Público; `{credential}` con un ID token real de Google Identity Services. |
| GET | `/users/me` | Sesión; perfil y tienda asignada. |
| GET / PUT | `/users/me/delivery-info` | Sesión; `{phone,address?,district?,agency?}`. |
| POST | `/users` | Administrador; alta de usuario por rol. |
| GET / POST | `/stores` | Administrador; listado o creación de tienda y operadora. |
| PUT | `/stores/{id}` | Administrador; nombre, dirección, encargada, distrito, provincia, departamento y teléfono. |
| PATCH | `/stores/{id}/operator` | Administrador; `{name?,email?,password?}`. Contraseña vacía conserva la anterior. |
| DELETE | `/stores/{id}` | Administrador; baja lógica, conserva historial y bloquea el acceso de su operadora. |

Una tienda se crea con `{name?,address?,managerName,district?,province?,department?,phone?,operatorEmail,operatorPassword}`. El código y la serie se asignan en el servidor.

## Catálogo, favoritos e inventario

| Método | Ruta | Acceso / finalidad |
| --- | --- | --- |
| GET | `/products`, `/products/{id}`, `/products/by-sku/{sku}` | Público; solo productos publicados. `totalStock` es el disponible después de reservas. |
| GET / POST | `/admin/products` | Administrador; todos los productos vigentes o creación. |
| GET / PUT / DELETE | `/admin/products/{id}` | Administrador; consulta, edición o baja lógica. |
| PATCH | `/admin/products/{id}/publication` | Administrador; `{published:true}`. |
| GET | `/catalog-metadata/colors`, `/catalog-metadata/garment-types` | Público; datos persistidos del editor. |
| PUT | `/admin/catalog-metadata/colors/{code}`, `/admin/catalog-metadata/garment-types/{code}` | Administrador; `{label}`. |
| POST | `/admin/media` | Administrador; multipart con campo `file`, imagen hasta 5 MB / 20 MP. Devuelve `{url}`. |
| GET | `/media/{name}` | Público; PNG reencodificado, nombre generado por servidor. |
| GET | `/favorites` | Cliente; IDs de sus productos favoritos. |
| PUT / DELETE | `/favorites/{productId}` | Cliente; añadir/quitar de forma idempotente. |
| GET / PATCH | `/admin/inventory` | Administrador; consulta o guardado transaccional de borrador. |

Alta/edición de producto:

```json
{
  "productCode":"CJTP",
  "name":"Conjunto Tiburón Princesa",
  "description":"Descripción",
  "category":"Conjuntos",
  "price":139.90,
  "published":true,
  "featured":false,
  "variants":[{"sku":"RS-CJTP-H-M-NEG-T1","gender":"H","size":"M","color":"NEG","totalStock":10}],
  "imageUrls":[]
}
```

La tienda `T1` y el color `NEG` deben existir. El stock de una variante nueva se inicializa con `totalStock`. Para variantes existentes, modificar inventario por su endpoint: editar un producto no sobrescribe reservas ni stock. Una imagen puede ser HTTPS o una URL devuelta por `/admin/media`.

El inventario devuelve `{sku,onHand,reserved,available,version}`. Guardar mediante `PATCH /admin/inventory` con `{"items":[{"sku":"...","onHand":20,"version":0}]}`. Ante 409, recargar el borrador. No reducir `onHand` por debajo de `reserved`.

## Promociones

`GET /promotions` es público y lista las activas. `GET /admin/promotions`, `POST /admin/promotions` y `DELETE /admin/promotions/{id}` requieren administrador. Crear con `{productId,discountAmount,endsAt?,maxUnits?}`. El servidor calcula el precio promocional y controla `unitsSold` y `reservedUnits`.

## Carrito, pedidos y pagos

El cliente puede consultar `GET /cart`, sustituirlo con `PUT /cart` (`{"items":[{"sku":"...","quantity":2}]}`) y vaciarlo con `DELETE /cart`. Guardar un carrito no reserva stock. El checkout reserva las unidades y vacía el carrito persistido en la misma transacción.

`POST /orders` requiere `Idempotency-Key` de 8 a 80 caracteres alfanuméricos, guion o guion bajo. Reutiliza la misma clave únicamente al reintentar la misma operación; un pedido nuevo usa una clave nueva. Un conflicto simultáneo de la clave puede responder 409: consultar/reintentar con la misma clave recupera el pedido ya creado.

```json
{
  "method":"motorizado",
  "fullName":"Cliente",
  "phone":"999999999",
  "address":"Calle 123",
  "district":"Lima",
  "items":[{"sku":"RS-CJTP-H-M-NEG-T1","quantity":2}]
}
```

Para `method:"shalom"`, indicar `agency`; para motorizado son obligatorios dirección y distrito. No enviar precios: el servidor determina producto, nombre, precio vigente, total y stock. Los datos de entrega se guardan en el perfil durante el primer checkout y no reemplazan los que el usuario haya editado posteriormente.

| Método | Ruta | Acceso / finalidad |
| --- | --- | --- |
| GET | `/orders`, `/orders/{id}` | Cliente; solo sus pedidos. |
| GET | `/orders/{id}/payment-instructions` | Cliente; beneficiario, imagen QR configurada, total y caducidad interna del pedido. |
| POST | `/orders/{id}/payment-submission` | Cliente; `{operationReference}`; pasa a revisión, no concede pago. |
| GET | `/orders/{id}/payment` | Cliente; revisión de su pago. |
| POST | `/orders/{id}/renew-qr` | Cliente; renueva un pedido vencido, recotiza y reserva de nuevo. |
| DELETE | `/orders/{id}` | Cliente; cancela un pedido sin pago. |
| GET | `/admin/orders` | Administrador; todos los pedidos. |
| GET | `/admin/orders/{id}/payment` | Administrador; operación enviada y revisión. |
| POST | `/admin/orders/{id}/confirm-payment` | Administrador; `{operationReference,amount}` contrastados con la transferencia real. |
| POST | `/admin/orders/{id}/reject-payment` | Administrador; `{reason}`; permite corregir una operación informada erróneamente. |
| POST | `/admin/orders/{id}/advance` | Administrador; avanza un paso de un pedido pagado. |
| DELETE | `/admin/orders/{id}` | Administrador; cancela según el estado, conserva el historial de pagos. |

Estados de pedido: `PENDING`, `EXPIRED`, `PAID`, `DELIVERED`, `CANCELLED`. Estado de revisión de pago: `PENDING`, `REVIEW`, `REJECTED`, `PAID`. El seguimiento se representa mediante `trackingStep` de 0 a 3. Caducidad y fechas usan el reloj del servidor; el corte de entrega usa `America/Lima`.

## Caja

`POST /operator/sales` requiere operadora activa e `Idempotency-Key`. La tienda, vendedora, serie y correlativo se obtienen de la sesión; no se aceptan como autoridad desde el cliente. Todos los SKU vendidos deben pertenecer a su tienda.

```json
{
  "customerName":"Clientes Varios",
  "customerDoc":"00000001",
  "paymentMethod":"mixto",
  "cashReceived":50.00,
  "qrAmount":100.00,
  "total":150.00,
  "discountReason":"Precio acordado por dos prendas",
  "items":[{"sku":"RS-CJTP-H-M-NEG-T1","quantity":2,"unitPrice":80.00}]
}
```

`unitPrice`, total negociado, razón y DNI son opcionales cuando no aplican. Con efectivo, `cashReceived` debe cubrir el total; con mixto, efectivo más QR deben coincidir exactamente. Con QR completo, no se envía efectivo y el servidor toma el total como `qrAmount`. La operadora confirma haber verificado el pago de caja; no se contacta una billetera bancaria desde este endpoint.

`GET /operator/sales?from=2026-11-01&to=2026-11-15&query=...` consulta ventas de su tienda, con fechas locales inclusivas y búsqueda por cliente, SKU, código de venta o boleta. `GET /operator/sales/{id}/receipt` devuelve la boleta y el snapshot de la venta para el puente local de impresión. Una operadora de otra tienda no puede consultar esa boleta.
