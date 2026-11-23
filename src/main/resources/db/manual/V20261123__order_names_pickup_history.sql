-- Apply once to the production MySQL database before deploying this version.
ALTER TABLE orders
    ADD COLUMN buyer_name VARCHAR(200) NULL,
    ADD COLUMN seller_name VARCHAR(200) NULL;

-- Backfill snapshots for existing orders where both accounts still exist.
UPDATE orders o
JOIN users buyer ON buyer.id = o.buyer_id
JOIN (
    SELECT oi.order_id, MIN(si.seller_id) AS seller_id
    FROM order_items oi
    JOIN sellable_items si ON si.id = oi.item_id
    GROUP BY oi.order_id
) sellers ON sellers.order_id = o.id
JOIN users seller ON seller.id = sellers.seller_id
SET o.buyer_name = buyer.name,
    o.seller_name = seller.name
WHERE o.buyer_name IS NULL OR o.seller_name IS NULL;

-- Existing six-digit PINs cannot be migrated to BCrypt hashes; force buyers to request a new PIN.
UPDATE orders SET pickup_pin = NULL WHERE pickup_pin IS NOT NULL;
ALTER TABLE orders CHANGE COLUMN pickup_pin pickup_pin_hash VARCHAR(100) NULL;

CREATE TABLE order_confirmed_events (
    order_id BIGINT NOT NULL PRIMARY KEY,
    buyer_name VARCHAR(200) NOT NULL,
    seller_name VARCHAR(200) NOT NULL,
    event_id BIGINT NULL,
    total_amount DECIMAL(12, 2) NOT NULL,
    occurred_on TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_order_confirmed_events_order
        FOREIGN KEY (order_id) REFERENCES orders(id)
);

