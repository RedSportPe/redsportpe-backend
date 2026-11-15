--
-- PostgreSQL database dump
--

-- Dumped from database version 17.4
-- Dumped by pg_dump version 17.4

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: auth_provider; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.auth_provider AS ENUM (
    'email',
    'google'
);


--
-- Name: order_method; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.order_method AS ENUM (
    'motorizado',
    'shalom',
    'tienda'
);


--
-- Name: order_payment_method; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.order_payment_method AS ENUM (
    'qr',
    'efectivo'
);


--
-- Name: print_job_status; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.print_job_status AS ENUM (
    'pending',
    'printed',
    'failed'
);


--
-- Name: user_role; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.user_role AS ENUM (
    'customer',
    'admin',
    'operator'
);


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: colors; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.colors (
    code character(3) NOT NULL,
    label character varying(40) NOT NULL
);


--
-- Name: commercial_agents; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.commercial_agents (
    user_id uuid NOT NULL,
    store_id uuid,
    commission_rate numeric(5,2) DEFAULT 0.00 NOT NULL
);


--
-- Name: delivery_info; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.delivery_info (
    user_id uuid NOT NULL,
    phone character varying(20),
    address character varying(200),
    district character varying(100),
    agency character varying(150)
);


--
-- Name: garment_types; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.garment_types (
    code character varying(30) NOT NULL,
    label character varying(60) NOT NULL,
    active boolean DEFAULT true
);


--
-- Name: inventory_units; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.inventory_units (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    variant_sku character varying(30) NOT NULL,
    warehouse_id uuid,
    correlativo integer NOT NULL,
    supplier_name character varying(150),
    received_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: order_items; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.order_items (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    order_id uuid,
    variant_sku character varying(30),
    product_id uuid,
    name_snapshot character varying(150) NOT NULL,
    image_snapshot character varying(500),
    size_snapshot character varying(4),
    color_snapshot character(3),
    unit_price numeric(10,2) NOT NULL,
    quantity integer NOT NULL
);


--
-- Name: order_shipping; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.order_shipping (
    order_id uuid NOT NULL,
    full_name character varying(150) NOT NULL,
    phone character varying(20),
    address character varying(200),
    district character varying(100)
);


--
-- Name: orders; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.orders (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    code character varying(12) NOT NULL,
    user_id uuid,
    total numeric(10,2) NOT NULL,
    method public.order_method NOT NULL,
    payment_method public.order_payment_method,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    qr_expires_at timestamp with time zone,
    paid_at timestamp with time zone,
    delivery_date date,
    tracking_step smallint DEFAULT 0,
    cash_received numeric(10,2),
    store_id uuid,
    operator_id uuid
);


--
-- Name: print_jobs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.print_jobs (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    variant_sku character varying(30) NOT NULL,
    quantity integer NOT NULL,
    requested_by uuid,
    store_id uuid,
    status public.print_job_status DEFAULT 'pending'::public.print_job_status,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    printed_at timestamp with time zone
);


--
-- Name: product_images; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.product_images (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    product_id uuid,
    url character varying(500) NOT NULL,
    "position" smallint DEFAULT 0
);


--
-- Name: products; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.products (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    product_code character varying(10) NOT NULL,
    name character varying(150) NOT NULL,
    description text,
    category character varying(60),
    price numeric(10,2) NOT NULL,
    published boolean DEFAULT false,
    featured boolean DEFAULT false,
    sales_count integer DEFAULT 0,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: promotions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.promotions (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    product_id uuid,
    discount_amount numeric(10,2) NOT NULL,
    ends_at date,
    max_units integer,
    units_sold integer DEFAULT 0,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    active boolean DEFAULT true
);


--
-- Name: receipts; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.receipts (
    order_id uuid NOT NULL,
    boleta_code character varying(15) NOT NULL,
    customer_name character varying(150) DEFAULT 'Clientes Varios'::character varying,
    customer_dni character varying(15),
    amount_in_words character varying(200) NOT NULL,
    printed_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    reprint_count integer DEFAULT 0
);


--
-- Name: stocks; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.stocks (
    variant_sku character varying(30) NOT NULL,
    warehouse_id uuid NOT NULL,
    quantity integer DEFAULT 0 NOT NULL
);


--
-- Name: store_operators; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.store_operators (
    store_id uuid NOT NULL,
    user_id uuid NOT NULL
);


--
-- Name: store_stocks; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.store_stocks (
    variant_sku character varying(30) NOT NULL,
    store_id uuid NOT NULL,
    quantity integer DEFAULT 0 NOT NULL
);


--
-- Name: stores; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.stores (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    name character varying(100) NOT NULL,
    code character varying(10) NOT NULL,
    address character varying(200),
    active boolean DEFAULT true,
    boleta_series character varying(10),
    manager_name character varying(150),
    district character varying(100),
    province character varying(100),
    department character varying(100),
    phone character varying(20)
);


--
-- Name: users; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.users (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    name character varying(120) NOT NULL,
    email character varying(160) NOT NULL,
    password_hash character varying(255),
    provider character varying(20) NOT NULL,
    role character varying(20) NOT NULL,
    points numeric(10,2) DEFAULT 0.00,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: variants; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.variants (
    sku character varying(30) NOT NULL,
    product_id uuid,
    gender character varying(2),
    size character varying(4),
    color character(3)
);


--
-- Name: warehouse_operators; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.warehouse_operators (
    warehouse_id uuid NOT NULL,
    user_id uuid NOT NULL,
    assigned_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: warehouses; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.warehouses (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    name character varying(100) NOT NULL,
    code character varying(10) NOT NULL,
    address character varying(200),
    active boolean DEFAULT true
);


--
-- Name: colors colors_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.colors
    ADD CONSTRAINT colors_pkey PRIMARY KEY (code);


--
-- Name: commercial_agents commercial_agents_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.commercial_agents
    ADD CONSTRAINT commercial_agents_pkey PRIMARY KEY (user_id);


--
-- Name: delivery_info delivery_info_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.delivery_info
    ADD CONSTRAINT delivery_info_pkey PRIMARY KEY (user_id);


--
-- Name: garment_types garment_types_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.garment_types
    ADD CONSTRAINT garment_types_pkey PRIMARY KEY (code);


--
-- Name: inventory_units inventory_units_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.inventory_units
    ADD CONSTRAINT inventory_units_pkey PRIMARY KEY (id);


--
-- Name: order_items order_items_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.order_items
    ADD CONSTRAINT order_items_pkey PRIMARY KEY (id);


--
-- Name: order_shipping order_shipping_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.order_shipping
    ADD CONSTRAINT order_shipping_pkey PRIMARY KEY (order_id);


--
-- Name: orders orders_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_code_key UNIQUE (code);


--
-- Name: orders orders_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_pkey PRIMARY KEY (id);


--
-- Name: print_jobs print_jobs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.print_jobs
    ADD CONSTRAINT print_jobs_pkey PRIMARY KEY (id);


--
-- Name: product_images product_images_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_images
    ADD CONSTRAINT product_images_pkey PRIMARY KEY (id);


--
-- Name: products products_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.products
    ADD CONSTRAINT products_pkey PRIMARY KEY (id);


--
-- Name: products products_product_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.products
    ADD CONSTRAINT products_product_code_key UNIQUE (product_code);


--
-- Name: promotions promotions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.promotions
    ADD CONSTRAINT promotions_pkey PRIMARY KEY (id);


--
-- Name: receipts receipts_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.receipts
    ADD CONSTRAINT receipts_pkey PRIMARY KEY (order_id);


--
-- Name: stocks stocks_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.stocks
    ADD CONSTRAINT stocks_pkey PRIMARY KEY (variant_sku, warehouse_id);


--
-- Name: store_operators store_operators_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.store_operators
    ADD CONSTRAINT store_operators_pkey PRIMARY KEY (store_id, user_id);


--
-- Name: store_stocks store_stocks_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.store_stocks
    ADD CONSTRAINT store_stocks_pkey PRIMARY KEY (variant_sku, store_id);


--
-- Name: stores stores_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.stores
    ADD CONSTRAINT stores_code_key UNIQUE (code);


--
-- Name: stores stores_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.stores
    ADD CONSTRAINT stores_pkey PRIMARY KEY (id);


--
-- Name: users users_email_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: variants variants_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.variants
    ADD CONSTRAINT variants_pkey PRIMARY KEY (sku);


--
-- Name: warehouse_operators warehouse_operators_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.warehouse_operators
    ADD CONSTRAINT warehouse_operators_pkey PRIMARY KEY (warehouse_id, user_id);


--
-- Name: warehouses warehouses_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.warehouses
    ADD CONSTRAINT warehouses_code_key UNIQUE (code);


--
-- Name: warehouses warehouses_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.warehouses
    ADD CONSTRAINT warehouses_pkey PRIMARY KEY (id);


--
-- Name: commercial_agents commercial_agents_store_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.commercial_agents
    ADD CONSTRAINT commercial_agents_store_id_fkey FOREIGN KEY (store_id) REFERENCES public.stores(id) ON DELETE SET NULL;


--
-- Name: commercial_agents commercial_agents_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.commercial_agents
    ADD CONSTRAINT commercial_agents_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: delivery_info delivery_info_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.delivery_info
    ADD CONSTRAINT delivery_info_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: inventory_units inventory_units_warehouse_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.inventory_units
    ADD CONSTRAINT inventory_units_warehouse_id_fkey FOREIGN KEY (warehouse_id) REFERENCES public.warehouses(id) ON DELETE CASCADE;


--
-- Name: order_items order_items_order_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.order_items
    ADD CONSTRAINT order_items_order_id_fkey FOREIGN KEY (order_id) REFERENCES public.orders(id) ON DELETE CASCADE;


--
-- Name: order_items order_items_product_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.order_items
    ADD CONSTRAINT order_items_product_id_fkey FOREIGN KEY (product_id) REFERENCES public.products(id) ON DELETE RESTRICT;


--
-- Name: order_items order_items_variant_sku_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.order_items
    ADD CONSTRAINT order_items_variant_sku_fkey FOREIGN KEY (variant_sku) REFERENCES public.variants(sku) ON DELETE RESTRICT;


--
-- Name: order_shipping order_shipping_order_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.order_shipping
    ADD CONSTRAINT order_shipping_order_id_fkey FOREIGN KEY (order_id) REFERENCES public.orders(id) ON DELETE CASCADE;


--
-- Name: orders orders_operator_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_operator_id_fkey FOREIGN KEY (operator_id) REFERENCES public.users(id) ON DELETE RESTRICT;


--
-- Name: orders orders_store_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_store_id_fkey FOREIGN KEY (store_id) REFERENCES public.stores(id) ON DELETE RESTRICT;


--
-- Name: orders orders_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE RESTRICT;


--
-- Name: print_jobs print_jobs_requested_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.print_jobs
    ADD CONSTRAINT print_jobs_requested_by_fkey FOREIGN KEY (requested_by) REFERENCES public.users(id) ON DELETE SET NULL;


--
-- Name: print_jobs print_jobs_store_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.print_jobs
    ADD CONSTRAINT print_jobs_store_id_fkey FOREIGN KEY (store_id) REFERENCES public.stores(id) ON DELETE CASCADE;


--
-- Name: product_images product_images_product_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_images
    ADD CONSTRAINT product_images_product_id_fkey FOREIGN KEY (product_id) REFERENCES public.products(id) ON DELETE CASCADE;


--
-- Name: promotions promotions_product_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.promotions
    ADD CONSTRAINT promotions_product_id_fkey FOREIGN KEY (product_id) REFERENCES public.products(id) ON DELETE CASCADE;


--
-- Name: stocks stocks_warehouse_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.stocks
    ADD CONSTRAINT stocks_warehouse_id_fkey FOREIGN KEY (warehouse_id) REFERENCES public.warehouses(id) ON DELETE CASCADE;


--
-- Name: store_operators store_operators_store_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.store_operators
    ADD CONSTRAINT store_operators_store_id_fkey FOREIGN KEY (store_id) REFERENCES public.stores(id) ON DELETE CASCADE;


--
-- Name: store_operators store_operators_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.store_operators
    ADD CONSTRAINT store_operators_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: store_stocks store_stocks_store_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.store_stocks
    ADD CONSTRAINT store_stocks_store_id_fkey FOREIGN KEY (store_id) REFERENCES public.stores(id) ON DELETE CASCADE;


--
-- Name: variants variants_color_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.variants
    ADD CONSTRAINT variants_color_fkey FOREIGN KEY (color) REFERENCES public.colors(code) ON DELETE RESTRICT;


--
-- Name: variants variants_product_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.variants
    ADD CONSTRAINT variants_product_id_fkey FOREIGN KEY (product_id) REFERENCES public.products(id) ON DELETE CASCADE;


--
-- Name: warehouse_operators warehouse_operators_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.warehouse_operators
    ADD CONSTRAINT warehouse_operators_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: warehouse_operators warehouse_operators_warehouse_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.warehouse_operators
    ADD CONSTRAINT warehouse_operators_warehouse_id_fkey FOREIGN KEY (warehouse_id) REFERENCES public.warehouses(id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--
