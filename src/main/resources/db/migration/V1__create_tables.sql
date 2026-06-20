CREATE TABLE orders (
    id                   VARCHAR(36)    NOT NULL,
    customer_id          VARCHAR(255)   NOT NULL,
    status               VARCHAR(50)    NOT NULL,
    total_amount         DECIMAL(19, 2) NOT NULL DEFAULT 0,
    payment_failure_count INT           NOT NULL DEFAULT 0,
    created_at           DATETIME(6)   NOT NULL,
    version              BIGINT         NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE order_items (
    id           BIGINT         NOT NULL AUTO_INCREMENT,
    order_id     VARCHAR(36)    NOT NULL,
    product_id   VARCHAR(255)   NOT NULL,
    product_name VARCHAR(255)   NOT NULL,
    quantity     INT            NOT NULL,
    unit_price   DECIMAL(19, 2) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE idempotency_keys (
    idempotency_key VARCHAR(255) NOT NULL,
    created_at      DATETIME(6)  NOT NULL,
    PRIMARY KEY (idempotency_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
