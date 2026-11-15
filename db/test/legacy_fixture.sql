-- Synthetic migration fixtures, never customer data.
INSERT INTO users(id,name,email,password_hash,provider,role) VALUES('00000000-0000-0000-0000-000000000001','Legacy Customer','legacy@example.test',NULL,'email','customer');
INSERT INTO stores(id,name,code,boleta_series) VALUES('00000000-0000-0000-0000-000000000002','Legacy Store','T1','B001');
INSERT INTO colors(code,label) VALUES('NEG','Negro');
INSERT INTO products(id,product_code,name,price,published) VALUES('00000000-0000-0000-0000-000000000003','LEGACY','Legacy Product',100,true);
INSERT INTO variants(sku,product_id,gender,size,color) VALUES('RS-LEGACY-H-M-NEG','00000000-0000-0000-0000-000000000003','H','M','NEG');
INSERT INTO store_stocks(variant_sku,store_id,quantity) VALUES('RS-LEGACY-H-M-NEG','00000000-0000-0000-0000-000000000002',7);
INSERT INTO orders(id,code,user_id,total,method,paid_at,tracking_step,store_id) VALUES('00000000-0000-0000-0000-000000000004','RS-OLD','00000000-0000-0000-0000-000000000001',100,'tienda',now(),3,'00000000-0000-0000-0000-000000000002');
INSERT INTO order_items(id,order_id,variant_sku,product_id,name_snapshot,unit_price,quantity) VALUES('00000000-0000-0000-0000-000000000005','00000000-0000-0000-0000-000000000004','RS-LEGACY-H-M-NEG','00000000-0000-0000-0000-000000000003','Legacy Product',100,1);
INSERT INTO receipts(order_id,boleta_code,amount_in_words) VALUES('00000000-0000-0000-0000-000000000004','B001-00000042','CIEN CON 00/100 SOLES');
