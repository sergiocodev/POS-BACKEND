-- V1__initial_schema.sql
-- Initial database indexes for performance optimization
-- Run this migration to add indexes that were missing from the Hibernate auto-DDL

-- Sale indexes
CREATE INDEX IF NOT EXISTS idx_sale_establishment_date ON sales (establishment_id, date);
CREATE INDEX IF NOT EXISTS idx_sale_sunat_status ON sales (sunat_status);
CREATE INDEX IF NOT EXISTS idx_sale_date ON sales (date);
CREATE INDEX IF NOT EXISTS idx_sale_customer ON sales (customer_id);
CREATE INDEX IF NOT EXISTS idx_sale_user ON sales (user_id);
CREATE INDEX IF NOT EXISTS idx_sale_cash_session ON sales (cash_session_id);
CREATE INDEX IF NOT EXISTS idx_sale_voided ON sales (is_voided);

-- Inventory indexes
CREATE INDEX IF NOT EXISTS idx_inventory_establishment ON inventory (establishment_id);
CREATE INDEX IF NOT EXISTS idx_inventory_lot ON inventory (lot_id);
CREATE INDEX IF NOT EXISTS idx_inventory_quantity ON inventory (quantity);

-- Product lot indexes
CREATE INDEX IF NOT EXISTS idx_lot_product ON product_lots (product_id);
CREATE INDEX IF NOT EXISTS idx_lot_expiry ON product_lots (expiry_date);

-- Stock movement indexes
CREATE INDEX IF NOT EXISTS idx_movement_establishment ON stock_movements (establishment_id);
CREATE INDEX IF NOT EXISTS idx_movement_lot ON stock_movements (lot_id);
CREATE INDEX IF NOT EXISTS idx_movement_created ON stock_movements (created_at);
CREATE INDEX IF NOT EXISTS idx_movement_type ON stock_movements (type);

-- Additional useful indexes for common query patterns
CREATE INDEX IF NOT EXISTS idx_purchase_establishment_date ON purchases (establishment_id, issue_date);
CREATE INDEX IF NOT EXISTS idx_purchase_supplier ON purchases (supplier_id);
CREATE INDEX IF NOT EXISTS idx_sale_item_sale ON sale_items (sale_id);
CREATE INDEX IF NOT EXISTS idx_sale_item_product ON sale_items (product_id);
CREATE INDEX IF NOT EXISTS idx_sale_item_lot ON sale_items (lot_id);
CREATE INDEX IF NOT EXISTS idx_sale_payment_sale ON sale_payments (sale_id);
CREATE INDEX IF NOT EXISTS idx_sale_payment_method ON sale_payments (payment_method);
CREATE INDEX IF NOT EXISTS idx_sale_payment_session ON sale_payments (sale_cash_session_id);
CREATE INDEX IF NOT EXISTS idx_account_receivable_status ON account_receivables (status);
CREATE INDEX IF NOT EXISTS idx_account_receivable_customer ON account_receivables (customer_id);
CREATE INDEX IF NOT EXISTS idx_account_payable_status ON account_payables (status);
CREATE INDEX IF NOT EXISTS idx_cash_session_user_status ON cash_sessions (user_id, status);
CREATE INDEX IF NOT EXISTS idx_voided_doc_establishment ON voided_documents (establishment_id);
CREATE INDEX IF NOT EXISTS idx_voided_doc_status ON voided_documents (sunat_status);
CREATE INDEX IF NOT EXISTS idx_stock_transfer_status ON stock_transfers (status);
CREATE INDEX IF NOT EXISTS idx_user_username ON users (username);
CREATE INDEX IF NOT EXISTS idx_user_email ON users (email);
CREATE INDEX IF NOT EXISTS idx_customer_document ON customers (document_number);
CREATE INDEX IF NOT EXISTS idx_product_code ON products (code);
CREATE INDEX IF NOT EXISTS idx_product_unit_barcode ON product_units (barcode);
CREATE INDEX IF NOT EXISTS idx_product_ingredient_product ON product_ingredients (product_id);
