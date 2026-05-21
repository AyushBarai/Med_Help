-- =====================================================================
-- V1__init_schema.sql
-- Flyway runs this automatically when the app starts for the first time.
-- File naming rule: V{number}__{description}.sql — version must increase.
-- =====================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";  -- enables uuid_generate_v4()

-- =====================================================================
-- LABS  (the "tenant" — every row belongs to one lab)
-- =====================================================================
CREATE TABLE labs (
    id                UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    name              VARCHAR(255) NOT NULL,
    slug              VARCHAR(100) NOT NULL UNIQUE,  -- used in patient portal URL
    owner_name        VARCHAR(255) NOT NULL,
    phone             VARCHAR(20)  NOT NULL UNIQUE,
    email             VARCHAR(255) UNIQUE,
    address           TEXT,
    city              VARCHAR(100),
    state             VARCHAR(100),
    gstin             VARCHAR(20),
    logo_url          VARCHAR(500),
    subscription_plan VARCHAR(20)  NOT NULL DEFAULT 'FREE',
    is_active         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- =====================================================================
-- LAB USERS  (staff accounts — OWNER, DOCTOR, TECHNICIAN, RECEPTIONIST)
-- =====================================================================
CREATE TABLE lab_users (
    id            UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    lab_id        UUID         NOT NULL REFERENCES labs(id) ON DELETE CASCADE,
    name          VARCHAR(255) NOT NULL,
    email         VARCHAR(255) NOT NULL,
    phone         VARCHAR(20),
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(30)  NOT NULL,   -- OWNER | DOCTOR | TECHNICIAN | RECEPTIONIST
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    UNIQUE(lab_id, email)
);

-- =====================================================================
-- PATIENTS  (registered by lab; identified by phone per lab)
-- =====================================================================
CREATE TABLE patients (
    id                   UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    lab_id               UUID         NOT NULL REFERENCES labs(id) ON DELETE CASCADE,
    name                 VARCHAR(255) NOT NULL,
    phone                VARCHAR(20)  NOT NULL,
    email                VARCHAR(255),
    dob                  DATE,
    gender               VARCHAR(10),   -- MALE | FEMALE | OTHER
    address              TEXT,
    portal_access_token  VARCHAR(255),  -- one-time token for patient portal login
    created_at           TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMP    NOT NULL DEFAULT NOW(),
    UNIQUE(lab_id, phone)              -- same phone can exist across different labs
);

-- =====================================================================
-- TEST CATALOG  (each lab defines their own test list with prices)
-- =====================================================================
CREATE TABLE test_catalog (
    id               UUID           PRIMARY KEY DEFAULT uuid_generate_v4(),
    lab_id           UUID           NOT NULL REFERENCES labs(id) ON DELETE CASCADE,
    name             VARCHAR(255)   NOT NULL,   -- "Complete Blood Count"
    code             VARCHAR(50),               -- "CBC"
    category         VARCHAR(100),              -- "Hematology"
    price            DECIMAL(10,2)  NOT NULL,
    turnaround_hours INTEGER        NOT NULL DEFAULT 24,
    sample_type      VARCHAR(100),              -- "Blood", "Urine", "Stool"
    reference_ranges JSONB,                     -- age/gender-based normal ranges
    is_active        BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP      NOT NULL DEFAULT NOW()
);

-- =====================================================================
-- TEST ORDERS  (a patient visit — can have multiple tests)
-- =====================================================================
CREATE TABLE test_orders (
    id                 UUID           PRIMARY KEY DEFAULT uuid_generate_v4(),
    lab_id             UUID           NOT NULL REFERENCES labs(id),
    patient_id         UUID           NOT NULL REFERENCES patients(id),
    order_number       VARCHAR(50)    NOT NULL UNIQUE,  -- "LAB-2024-0001"
    referred_by        VARCHAR(255),                    -- referring doctor name
    collected_by       UUID           REFERENCES lab_users(id),
    collection_type    VARCHAR(20)    NOT NULL DEFAULT 'WALK_IN',  -- WALK_IN | HOME_COLLECTION
    collection_address TEXT,
    status             VARCHAR(30)    NOT NULL DEFAULT 'REGISTERED',
    -- status flow: REGISTERED -> SAMPLE_COLLECTED -> PROCESSING -> REPORT_READY -> DELIVERED
    total_amount       DECIMAL(10,2)  NOT NULL,
    discount_amount    DECIMAL(10,2)  NOT NULL DEFAULT 0,
    net_amount         DECIMAL(10,2)  NOT NULL,
    notes              TEXT,
    created_at         TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMP      NOT NULL DEFAULT NOW(),
    expected_at        TIMESTAMP,
    delivered_at       TIMESTAMP
);

-- =====================================================================
-- ORDER ITEMS  (which specific tests are in each order)
-- =====================================================================
CREATE TABLE order_items (
    id         UUID          PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id   UUID          NOT NULL REFERENCES test_orders(id) ON DELETE CASCADE,
    test_id    UUID          NOT NULL REFERENCES test_catalog(id),
    price      DECIMAL(10,2) NOT NULL,   -- snapshot of price at time of order
    status     VARCHAR(20)   NOT NULL DEFAULT 'PENDING',  -- PENDING | PROCESSING | COMPLETED
    created_at TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- =====================================================================
-- TEST RESULTS  (parameter-wise values entered by technician)
-- =====================================================================
CREATE TABLE test_results (
    id              UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_item_id   UUID         NOT NULL REFERENCES order_items(id) ON DELETE CASCADE,
    parameter_name  VARCHAR(255) NOT NULL,   -- "Hemoglobin", "WBC Count"
    value           VARCHAR(100),            -- "12.5"
    unit            VARCHAR(50),             -- "g/dL"
    reference_range VARCHAR(100),            -- "12.0 - 17.0"
    flag            VARCHAR(20)  DEFAULT 'NORMAL',  -- NORMAL | LOW | HIGH | CRITICAL
    entered_by      UUID         REFERENCES lab_users(id),
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- =====================================================================
-- REPORTS  (generated PDF linked to an order)
-- =====================================================================
CREATE TABLE reports (
    id           UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id     UUID         NOT NULL REFERENCES test_orders(id),
    version      INTEGER      NOT NULL DEFAULT 1,  -- allows report corrections
    pdf_url      VARCHAR(500),                      -- S3 URL
    access_token VARCHAR(255) UNIQUE,               -- for shareable public link
    is_final     BOOLEAN      NOT NULL DEFAULT FALSE,
    generated_at TIMESTAMP,
    generated_by UUID         REFERENCES lab_users(id),
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- =====================================================================
-- PAYMENTS  (cash/UPI/card/online)
-- =====================================================================
CREATE TABLE payments (
    id                  UUID          PRIMARY KEY DEFAULT uuid_generate_v4(),
    lab_id              UUID          NOT NULL REFERENCES labs(id),
    order_id            UUID          NOT NULL REFERENCES test_orders(id),
    amount              DECIMAL(10,2) NOT NULL,
    method              VARCHAR(20)   NOT NULL,  -- CASH | UPI | CARD | ONLINE
    razorpay_payment_id VARCHAR(100),
    status              VARCHAR(20)   NOT NULL DEFAULT 'PENDING',  -- PENDING | PAID | REFUNDED
    paid_at             TIMESTAMP,
    created_at          TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- =====================================================================
-- NOTIFICATION LOGS  (track every WhatsApp/SMS/email sent)
-- =====================================================================
CREATE TABLE notification_logs (
    id         UUID      PRIMARY KEY DEFAULT uuid_generate_v4(),
    lab_id     UUID      NOT NULL REFERENCES labs(id),
    order_id   UUID      REFERENCES test_orders(id),
    patient_id UUID      REFERENCES patients(id),
    channel    VARCHAR(20) NOT NULL,  -- WHATSAPP | SMS | EMAIL
    type       VARCHAR(30) NOT NULL,  -- ORDER_CONFIRM | REPORT_READY | PAYMENT_REMINDER
    status     VARCHAR(20) NOT NULL DEFAULT 'PENDING',  -- PENDING | SENT | DELIVERED | FAILED
    sent_at    TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =====================================================================
-- INDEXES  (speeds up the most common queries)
-- =====================================================================
CREATE INDEX idx_lab_users_lab_id          ON lab_users(lab_id);
CREATE INDEX idx_patients_lab_id           ON patients(lab_id);
CREATE INDEX idx_patients_phone            ON patients(phone);
CREATE INDEX idx_test_catalog_lab_id       ON test_catalog(lab_id);
CREATE INDEX idx_test_orders_lab_id        ON test_orders(lab_id);
CREATE INDEX idx_test_orders_patient_id    ON test_orders(patient_id);
CREATE INDEX idx_test_orders_status        ON test_orders(status);
CREATE INDEX idx_test_orders_created_at    ON test_orders(created_at);
CREATE INDEX idx_order_items_order_id      ON order_items(order_id);
CREATE INDEX idx_test_results_order_item   ON test_results(order_item_id);
CREATE INDEX idx_reports_order_id          ON reports(order_id);
CREATE INDEX idx_reports_access_token      ON reports(access_token);
CREATE INDEX idx_payments_order_id         ON payments(order_id);
CREATE INDEX idx_notification_logs_order   ON notification_logs(order_id);