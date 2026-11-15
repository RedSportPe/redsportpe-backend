-- Manual, transactional migration for PostgreSQL 17. NEVER run automatically on startup.
-- Apply 000_existing_schema.sql ONLY to an EMPTY database; existing installations start here.
BEGIN;
SET LOCAL search_path TO public;
SELECT pg_advisory_xact_lock(726377001);
CREATE TABLE IF NOT EXISTS backend_schema_migrations(version varchar(20) PRIMARY KEY,applied_at timestamptz NOT NULL DEFAULT now());
DO $$ BEGIN
 IF EXISTS(SELECT 1 FROM backend_schema_migrations WHERE version='001') THEN
   RAISE EXCEPTION 'Migration 001 already applied; no changes made';
 END IF;
 IF EXISTS(SELECT lower(trim(email)) FROM users GROUP BY lower(trim(email)) HAVING count(*)>1) THEN
   RAISE EXCEPTION 'Resolve duplicate normalized emails before migrating';
 END IF;
END $$;
ALTER TYPE order_payment_method ADD VALUE IF NOT EXISTS 'mixto';
ALTER TABLE products ADD COLUMN deleted boolean NOT NULL DEFAULT false, ADD COLUMN version bigint NOT NULL DEFAULT 0;
UPDATE products SET sales_count=COALESCE(sales_count,0),published=COALESCE(published,false),featured=COALESCE(featured,false),created_at=COALESCE(created_at,now());
ALTER TABLE variants ALTER COLUMN sku TYPE varchar(60);
ALTER TABLE order_items ALTER COLUMN variant_sku TYPE varchar(60);
ALTER TABLE users ADD COLUMN google_subject varchar(255) UNIQUE;
UPDATE users SET email=lower(trim(email)),points=COALESCE(points,0);
ALTER TABLE users ALTER COLUMN points SET NOT NULL;
CREATE UNIQUE INDEX users_normalized_email ON users(lower(email));
CREATE UNIQUE INDEX one_operator_per_store ON store_operators(store_id);
CREATE UNIQUE INDEX one_store_per_operator ON store_operators(user_id);
CREATE UNIQUE INDEX unique_boleta_series ON stores(boleta_series);
ALTER TABLE delivery_info ALTER COLUMN agency TYPE varchar(200);
ALTER TABLE promotions ADD COLUMN reserved_units integer NOT NULL DEFAULT 0, ADD COLUMN cancelled boolean NOT NULL DEFAULT false, ADD COLUMN version bigint NOT NULL DEFAULT 0;
UPDATE promotions SET units_sold=COALESCE(units_sold,0),active=COALESCE(active,true),cancelled=NOT COALESCE(active,true);
ALTER TABLE promotions ALTER COLUMN units_sold SET NOT NULL,ALTER COLUMN active SET NOT NULL;
ALTER TABLE promotions ADD CONSTRAINT promotion_counts CHECK(units_sold>=0 AND reserved_units>=0 AND (max_units IS NULL OR units_sold+reserved_units<=max_units));
CREATE INDEX promotions_product_lookup ON promotions(product_id);
CREATE TABLE inventory_stock(sku varchar(60) PRIMARY KEY,on_hand integer NOT NULL,reserved integer NOT NULL DEFAULT 0,version bigint NOT NULL DEFAULT 0,CHECK(on_hand>=0 AND reserved>=0 AND reserved<=on_hand));
-- Legacy five-part SKUs remain intact for historical foreign keys. Store variants
-- are copied with their store suffix; old warehouse stock is NEVER guessed into a store.
INSERT INTO variants(sku,product_id,gender,size,color)
 SELECT ss.variant_sku||'-'||s.code,v.product_id,v.gender,v.size,v.color
 FROM store_stocks ss JOIN stores s ON s.id=ss.store_id JOIN variants v ON v.sku=ss.variant_sku
 WHERE ss.variant_sku !~ '-T[1-9][0-9]*$'
 ON CONFLICT(sku) DO NOTHING;
INSERT INTO inventory_stock(sku,on_hand)
 SELECT CASE WHEN ss.variant_sku ~ '-T[1-9][0-9]*$' THEN ss.variant_sku ELSE ss.variant_sku||'-'||s.code END,ss.quantity
 FROM store_stocks ss JOIN stores s ON s.id=ss.store_id JOIN variants v ON v.sku=ss.variant_sku;
INSERT INTO inventory_stock(sku,on_hand) SELECT sku,0 FROM variants WHERE sku ~ '-T[1-9][0-9]*$' ON CONFLICT(sku) DO NOTHING;
CREATE TABLE sequence_counters(name varchar(80) PRIMARY KEY,counter_value bigint NOT NULL DEFAULT 0);
INSERT INTO sequence_counters(name,counter_value) SELECT 'stores',COALESCE(max(substring(code from 2)::bigint),0) FROM stores;
INSERT INTO sequence_counters(name,counter_value)
 SELECT 'receipt:'||s.code,COALESCE(max(substring(r.boleta_code from '-([0-9]+)$')::bigint),0)
 FROM stores s LEFT JOIN orders o ON o.store_id=s.id LEFT JOIN receipts r ON r.order_id=o.id GROUP BY s.code;
ALTER TABLE orders ALTER COLUMN code TYPE varchar(40), ADD COLUMN request_key varchar(80),ADD COLUMN status varchar(20),
 ADD COLUMN subtotal numeric(12,2),ADD COLUMN discount_reason varchar(300),ADD COLUMN customer_name varchar(150),
 ADD COLUMN customer_doc varchar(20),ADD COLUMN phone varchar(20),ADD COLUMN address varchar(200),ADD COLUMN district varchar(100),
 ADD COLUMN agency varchar(200),ADD COLUMN qr_amount numeric(12,2),ADD COLUMN seller_name varchar(150),ADD COLUMN store_code varchar(10),
 ADD COLUMN boleta_number varchar(30) UNIQUE,ADD COLUMN version bigint NOT NULL DEFAULT 0;
UPDATE orders o SET customer_name=sh.full_name,phone=sh.phone,address=sh.address,district=sh.district FROM order_shipping sh WHERE sh.order_id=o.id;
UPDATE orders o SET store_code=s.code FROM stores s WHERE s.id=o.store_id;
UPDATE orders o SET seller_name=u.name FROM users u WHERE u.id=o.operator_id;
UPDATE orders o SET boleta_number=r.boleta_code,customer_name=COALESCE(o.customer_name,r.customer_name),customer_doc=r.customer_dni FROM receipts r WHERE r.order_id=o.id;
-- Legacy unpaid orders did not reserve stock. Keep their history and force a fresh quote.
UPDATE orders SET request_key='legacy-'||id,status=CASE WHEN paid_at IS NULL THEN 'EXPIRED' WHEN method='tienda' OR tracking_step=3 THEN 'DELIVERED' ELSE 'PAID' END,
 subtotal=total,customer_name=COALESCE(customer_name,'Clientes Varios'),created_at=COALESCE(created_at,now()),
 qr_expires_at=COALESCE(qr_expires_at,created_at,now()),tracking_step=COALESCE(tracking_step,0);
ALTER TABLE orders ALTER COLUMN request_key SET NOT NULL,ALTER COLUMN status SET NOT NULL,ALTER COLUMN subtotal SET NOT NULL,
 ALTER COLUMN customer_name SET NOT NULL,ALTER COLUMN created_at SET NOT NULL,ALTER COLUMN qr_expires_at SET NOT NULL,ALTER COLUMN tracking_step SET NOT NULL;
ALTER TABLE orders ADD CONSTRAINT order_idempotency UNIQUE(user_id,request_key),ADD CONSTRAINT valid_order_status CHECK(status IN('PENDING','EXPIRED','PAID','DELIVERED','CANCELLED')),
 ADD CONSTRAINT valid_tracking CHECK(tracking_step BETWEEN 0 AND 3);
ALTER TABLE order_items ADD COLUMN catalog_price numeric(12,2),ADD COLUMN promotion_id uuid,ADD COLUMN line_position integer;
UPDATE order_items SET catalog_price=unit_price;
WITH positions AS(SELECT id,row_number() OVER(PARTITION BY order_id ORDER BY id)-1 AS n FROM order_items)
 UPDATE order_items i SET line_position=p.n FROM positions p WHERE p.id=i.id;
ALTER TABLE order_items ALTER COLUMN catalog_price SET NOT NULL;
CREATE INDEX orders_customer_created ON orders(user_id,created_at DESC);
CREATE INDEX orders_store_created ON orders(store_code,created_at DESC);
CREATE INDEX pending_order_expiration ON orders(qr_expires_at) WHERE status='PENDING';
CREATE INDEX order_product_lookup ON order_items(product_id,order_id);
CREATE TABLE payment_reviews(order_id uuid PRIMARY KEY REFERENCES orders(id),status varchar(20) NOT NULL,
 operation_reference varchar(100) UNIQUE,amount numeric(12,2),submitted_at timestamptz,confirmed_at timestamptz,reviewed_by varchar(160),review_note varchar(300));
INSERT INTO payment_reviews(order_id,status,amount,confirmed_at) SELECT id,CASE WHEN paid_at IS NULL THEN 'PENDING' ELSE 'PAID' END,total,paid_at FROM orders WHERE method<>'tienda';
CREATE TABLE favorites(id uuid PRIMARY KEY,user_id uuid NOT NULL REFERENCES users(id),product_id uuid NOT NULL REFERENCES products(id),UNIQUE(user_id,product_id));
CREATE TABLE saved_cart_lines(id uuid PRIMARY KEY,user_id uuid NOT NULL REFERENCES users(id),sku varchar(60) NOT NULL,quantity integer NOT NULL CHECK(quantity>0),UNIQUE(user_id,sku));
ALTER TABLE receipts ALTER COLUMN boleta_code TYPE varchar(30), ADD COLUMN store_address varchar(200);
INSERT INTO backend_schema_migrations(version) VALUES('001');
COMMIT;
