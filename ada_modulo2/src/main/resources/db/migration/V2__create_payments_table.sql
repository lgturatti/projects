CREATE TABLE payments (
    id             VARCHAR(36)  NOT NULL,
    order_id       VARCHAR(36)  NOT NULL,
    payment_method VARCHAR(100) NOT NULL,
    status         VARCHAR(20)  NOT NULL,
    created_at     DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES orders (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
