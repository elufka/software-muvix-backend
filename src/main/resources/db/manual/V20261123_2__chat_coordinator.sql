-- Apply once to the production MySQL database before deploying this version.
ALTER TABLE offers
    ADD COLUMN counter_amount DECIMAL(12, 2) NULL,
    ADD COLUMN counter_accepted BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE escrow_accounts ADD COLUMN checkout_url VARCHAR(2000) NULL;

ALTER TABLE orders ADD COLUMN created_at TIMESTAMP(6) NULL;
UPDATE orders o LEFT JOIN order_confirmed_events e ON e.order_id=o.id
SET o.created_at=COALESCE(e.occurred_on,CURRENT_TIMESTAMP(6)) WHERE o.created_at IS NULL;
ALTER TABLE orders MODIFY COLUMN created_at TIMESTAMP(6) NOT NULL;

CREATE TABLE chat_message_templates (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(20) NOT NULL,
    title VARCHAR(160) NOT NULL,
    message VARCHAR(2000) NOT NULL,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE chat_messages (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NULL,
    offer_id BIGINT NULL,
    template_id BIGINT NULL,
    sender_id BIGINT NULL,
    type VARCHAR(40) NOT NULL,
    content VARCHAR(4000) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    proposal_id BIGINT NULL,
    INDEX idx_chat_order_created (order_id, created_at),
    INDEX idx_chat_offer_created (offer_id, created_at),
    CONSTRAINT fk_chat_messages_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_chat_messages_offer FOREIGN KEY (offer_id) REFERENCES offers(id),
    CONSTRAINT fk_chat_messages_template FOREIGN KEY (template_id) REFERENCES chat_message_templates(id)
);

CREATE TABLE pickup_coordinations (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    proposer_id BIGINT NOT NULL,
    start_time TIMESTAMP(6) NOT NULL,
    end_time TIMESTAMP(6) NOT NULL,
    authorized_person_name VARCHAR(200) NULL,
    authorized_person_doc VARCHAR(100) NULL,
    authorized_person_phone VARCHAR(50) NULL,
    status VARCHAR(30) NOT NULL,
    buyer_agreed_in_chat BOOLEAN NOT NULL DEFAULT FALSE,
    seller_agreed_in_chat BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP(6) NOT NULL,
    INDEX idx_pickup_coord_order (order_id, id),
    CONSTRAINT fk_pickup_coord_order FOREIGN KEY (order_id) REFERENCES orders(id)
);

CREATE TABLE pickup_coordination_confirmations (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    proposal_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    accepted BOOLEAN NOT NULL,
    responded_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT uk_pickup_coord_confirm_actor UNIQUE (proposal_id, user_id),
    CONSTRAINT fk_pickup_coord_confirm_proposal FOREIGN KEY (proposal_id) REFERENCES pickup_coordinations(id)
);
