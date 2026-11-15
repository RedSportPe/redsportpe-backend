# RedSportPe Backend

API Java 21 / Spring Boot 4.1 / PostgreSQL 17 para el catálogo, tiendas, inventario, pedidos, pagos y caja. Mantiene la estructura DDD del proyecto: `domain`, `application`, `infrastructure` e `interfaces` dentro de cada bounded context.

## Arranque

1. Revisa [la guía de migración](docs/DATABASE.md). Una instalación existente aplica **solo** `db/migrations/001_complete_backend.sql`. Una base vacía requiere primero `db/baseline/000_existing_schema.sql`. La aplicación nunca ejecuta estos scripts automáticamente.
2. Configura `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` y `JWT_SECRET` en el entorno del proceso. `application-local.properties` sigue excluido de Git y se puede activar mediante el perfil `local`. Un archivo `.env` no se carga automáticamente.
3. Ejecuta `./mvnw spring-boot:run` o, en Windows, `./mvnw.cmd spring-boot:run`.

Ejemplo PowerShell, usando valores propios:

```powershell
$env:DB_URL = 'jdbc:postgresql://localhost:5432/redsport'
$env:DB_USERNAME = 'postgres'
$env:DB_PASSWORD = '<contraseña local>'
$env:JWT_SECRET = '<secreto aleatorio persistente de al menos 32 bytes>'
.\mvnw.cmd spring-boot:run
```

Para generar un secreto una vez, usa `[Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(64))` y guárdalo en el gestor de secretos del despliegue. El mismo secreto debe mantenerse entre reinicios.

Swagger: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html). Contrato JSON: `/v3/api-docs`. [Referencia de endpoints y ejemplos](docs/API.md).

## Configuración

| Variable | Uso |
| --- | --- |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | Conexión PostgreSQL |
| `JWT_SECRET` | Firma JWT, mínimo 32 bytes; sin valor incorporado |
| `CORS_ALLOWED_ORIGINS` | Orígenes separados por coma; predeterminado `http://localhost:4200` |
| `GOOGLE_CLIENT_ID` | Client ID de Google Identity Services; sin él, Google devuelve 503 |
| `PAYMENT_QR_IMAGE_URL` | URL del QR comercial real, servida por tu comercio/proveedor |
| `PAYMENT_RECIPIENT` | Beneficiario que el cliente debe comprobar |
| `SEED_ADMIN_ENABLED` | `true` habilita la creación inicial de un administrador; predeterminado `false` |
| `SEED_ADMIN_EMAIL`, `SEED_ADMIN_PASSWORD` | Credenciales iniciales; contraseña de al menos 12 caracteres |

El seed no reemplaza administradores existentes ni imprime contraseñas. Desactívalo después del alta inicial. La caducidad del token se configura mediante `jwt.expiration-days`.

## Flujos implementados

- Registro exclusivo de clientes, login JWT, Google mediante verificación del ID token, perfil y datos de entrega persistidos. Las operadoras se autentican únicamente si tienen una tienda activa.
- Alta y edición de tiendas, creación/actualización de su operadora y baja lógica. Los códigos y series no se reutilizan.
- Catálogo público y administración de productos, variantes, publicación, imágenes, colores y tipos de prenda. Los SKU existentes se conservan para mantener referencias históricas. Favoritos por cliente.
- Inventario por SKU y tienda: existencia física, reserva y disponible; cambios administrativos con versión para detectar borradores obsoletos.
- Carrito persistido, checkout con precios calculados en servidor, pedidos por cliente, cancelación, vencimiento a las ocho horas y renovación con una cotización actualizada.
- Promociones por producto, importe fijo, fin de día en Lima y cupo que considera tanto ventas como reservas. Si un carrito pide más unidades de las que quedan en promoción, se devuelve 409 para que el cliente ajuste la cantidad.
- Revisión manual de pagos QR: envío de operación, confirmación/rechazo administrativo, importes contrastados y operación única. Repetir la confirmación no duplica stock, ventas ni puntos.
- Caja de tienda con efectivo, QR y mixto; precios negociados y total negociado con motivo obligatorio; boleta persistida, correlativo concurrente y monto en letras. Las promociones automáticas no requieren un motivo de negociación. Las ventas POS no acreditan puntos a la operadora.
- Seguimiento de cuatro pasos administrado desde el backend y fechas de entrega con corte a las 15:00 de Lima.

La reserva se descuenta del disponible al crear el pedido. Al pagarlo se convierte en salida física. Vencer o cancelar un pedido pendiente libera sus reservas. Una cancelación administrativa después del pago conserva puntos e historial; no genera un reembolso ni una entrada automática de mercadería.

## Pruebas

```powershell
.\scripts\Test-Backend.ps1
```

El script requiere los binarios de PostgreSQL 17 y Java 21. `-PgBin` permite indicar otra carpeta de instalación. Inicializa un clúster temporal bajo `target/`, en un puerto aleatorio de `127.0.0.1`, importa el esquema, migra datos sintéticos y comprueba que se conservan pedidos, SKU, existencias y correlativos. Después ejecuta pruebas de dominio, integración HTTP/transaccional, concurrencia, Google e imágenes. Detiene su servidor al finalizar. No utiliza la base configurada para la aplicación.

`./mvnw test` ejecuta las pruebas unitarias sin PostgreSQL; las de integración se habilitan exclusivamente con `TEST_DATABASE_URL` apuntando a la instancia aislada `redsport_test`. Para verificar todo, utiliza el script.

Para empaquetar, `./mvnw package` produce el JAR ejecutable en `target/`. El archivo privado `application-local.properties` se excluye del JAR; un despliegue usa variables de entorno o configuración externa.

## Integración pendiente de entorno y frontend

Los endpoints están implementados y probados; no se han aplicado las migraciones a la base de trabajo. Para usar el backend con ella, sigue `docs/DATABASE.md` y configura el entorno.

El frontend existente conserva simulaciones en registro/Google, catálogo, promociones, carrito, pedidos y POS. Debe consumir estos endpoints y los valores de `points`/`storeCode` entregados por el servidor para sustituir esas simulaciones. Este cambio del backend no modifica el frontend.

Los pagos se verifican manualmente por el administrador: no se simula una confirmación bancaria. La vigencia de ocho horas corresponde al pedido y su reserva; un QR comercial estático no puede impedir que una billetera externa acepte una transferencia tardía. No se han contratado/conectado una pasarela bancaria, la API de Shalom, facturación SUNAT, mensajería ni almacenamiento CDN. La boleta es un comprobante interno de venta, no una emisión electrónica SUNAT. La impresión continúa a cargo del puente local del frontend. El despliegue y las credenciales reales de Google/comercio/proveedores pertenecen a la configuración del entorno.

La verificación de Google utiliza la [biblioteca oficial y las comprobaciones de firma, audiencia, emisor y caducidad recomendadas por Google](https://developers.google.com/identity/gsi/web/guides/verify-google-id-token). El backend no vincula una cuenta de contraseña existente solo porque Google entregue el mismo correo. Springdoc se actualizó a la rama 3.x, [compatible con Spring Boot 4](https://springdoc.org/).
