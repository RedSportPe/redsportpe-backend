DO $$ BEGIN
 IF (SELECT count(*) FROM orders WHERE code='RS-OLD' AND status='DELIVERED' AND boleta_number='B001-00000042')<>1 THEN RAISE EXCEPTION 'Legacy order lost'; END IF;
 IF (SELECT on_hand FROM inventory_stock WHERE sku='RS-LEGACY-H-M-NEG-T1')<>7 THEN RAISE EXCEPTION 'Legacy stock lost'; END IF;
 IF (SELECT count(*) FROM variants WHERE sku='RS-LEGACY-H-M-NEG')<>1 THEN RAISE EXCEPTION 'Historical SKU lost'; END IF;
 IF (SELECT counter_value FROM sequence_counters WHERE name='receipt:T1')<>42 THEN RAISE EXCEPTION 'Receipt sequence restarted'; END IF;
 IF (SELECT count(*) FROM order_items WHERE variant_sku='RS-LEGACY-H-M-NEG')<>1 THEN RAISE EXCEPTION 'Historical reference lost'; END IF;
END $$;
