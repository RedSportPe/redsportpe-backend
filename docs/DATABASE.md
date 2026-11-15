# Esquema y migración

`spring.jpa.hibernate.ddl-auto=validate` permanece activo. Hibernate verifica el mapeo y no crea, actualiza ni elimina tablas. No se incorporó un migrador automático de arranque.

## Archivos

- `db/baseline/000_existing_schema.sql`: copia **solo del esquema** PostgreSQL 17 existente, sin datos ni credenciales. Se usa para instalar una base vacía y para pruebas. No se ejecuta encima de una instalación existente.
- `db/migrations/001_complete_backend.sql`: evolución transaccional del esquema anterior. Registra su versión en `backend_schema_migrations`. Un segundo intento aborta sin modificar datos.
- `db/test/`: datos sintéticos y aserciones de conservación de información; no forman parte de una instalación real.

## Aplicación a una base existente

1. Detén los procesos que escriben en la base y conserva un respaldo completo con `pg_dump`. No uses los fixtures de prueba en una base de trabajo.
2. Comprueba que la estructura coincide con el baseline. Revisa los índices únicos de correo normalizado, operadora por tienda, tienda por operadora y serie de boleta. Si existen duplicados, resuélvelos explícitamente; el script aborta en vez de borrar datos para hacerlos encajar.
3. Ejecuta `psql -X -v ON_ERROR_STOP=1 -d <base> -f db/migrations/001_complete_backend.sql` utilizando tus parámetros de conexión. No pongas contraseñas en argumentos ni en el repositorio.
4. Confirma la fila `001` en `backend_schema_migrations` y arranca el backend con `validate`. Verifica login, catálogo e inventario antes de habilitar ventas.

La migración no se aplicó a la base local del usuario durante este trabajo. Se probó sobre PostgreSQL 17 aislado, primero con el esquema vacío y después con fixtures que incluyen un SKU antiguo, stock de tienda, pedido, línea histórica y boleta número 42.

## Decisiones de compatibilidad

- Se reutilizan `users`, `stores`, `store_operators`, `products`, `variants`, `product_images`, `colors`, `garment_types`, `delivery_info`, `orders`, `order_items`, `promotions` y `receipts`.
- `orders` recibe estado, clave de idempotencia, datos de entrega y snapshots de caja. Se copian los datos existentes de `order_shipping`, tienda y boleta; las tablas antiguas se conservan. Los códigos de pedido se amplían para alojar UUID.
- Los pedidos antiguos sin pago quedan `EXPIRED`: el backend anterior no reservaba sus unidades. Renovarlos genera una cotización y una reserva nuevas. Los pedidos pagados conservan su seguimiento y boleta.
- Los SKU antiguos de cinco segmentos se conservan para no romper claves foráneas históricas. Para cada fila de `store_stocks` se crea, cuando corresponde, una variante con sufijo `Tn` y se importa su cantidad a `inventory_stock`.
- A partir de este backend, `inventory_stock` es la fuente de existencia/reserva por SKU de tienda. `store_stocks`, `stocks`, almacenes e inventario de unidades antiguo se conservan como legado; no se siguen escribiendo ni se interpreta el stock de un almacén como stock de una tienda. Cualquier sistema antiguo que todavía escriba esas tablas debe detenerse antes del cambio.
- El contador `stores` parte del mayor código de tienda existente. Cada contador `receipt:Tn` parte del mayor correlativo de sus boletas. La creación de tiendas y las ventas bloquean su contador dentro de la transacción.
- Los productos se eliminan lógicamente. Un producto con pedidos activos no se puede eliminar. Las variantes existentes no se retiran: se ajusta su stock a cero para mantener la trazabilidad.
- Se añade `mixto` al enum de pago sin reemplazar los valores previos. Los valores se escriben con casts PostgreSQL explícitos; el mapeo se verificó con `validate` sobre PostgreSQL real.

## Recuperación

Un error durante la migración provoca rollback de su transacción. Después de habilitar ventas en el nuevo esquema, no ejecutes un script inverso que elimine columnas, reservas o boletas. Para volver al sistema anterior, detén escrituras, conserva un respaldo del estado nuevo y restaura el respaldo anterior en una base separada; reconcilia las operaciones realizadas desde el cambio antes de reabrir.
