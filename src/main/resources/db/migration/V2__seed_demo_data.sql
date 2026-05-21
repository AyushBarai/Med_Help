-- =====================================================================
-- V2__seed_demo_data.sql
-- Creates a demo lab + owner user so you can test login immediately.
-- Password for demo@pathlab.com is:  Admin@123
-- =====================================================================

INSERT INTO labs (id, name, slug, owner_name, phone, email, city, state, subscription_plan)
VALUES (
    'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
    'Apollo Diagnostics Demo',
    'apollo-demo',
    'Dr. Rajesh Kumar',
    '9876543210',
    'demo@pathlab.com',
    'Mumbai',
    'Maharashtra',
    'FREE'
);

-- BCrypt hash of "Admin@123" (strength 10)
-- You can generate a new one at: https://bcrypt-generator.com
INSERT INTO lab_users (lab_id, name, email, phone, password_hash, role)
VALUES (
    'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
    'Dr. Rajesh Kumar',
    'demo@pathlab.com',
    '9876543210',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh/y',
    'OWNER'
);

-- A few sample tests in the catalog
INSERT INTO test_catalog (lab_id, name, code, category, price, turnaround_hours, sample_type, reference_ranges)
VALUES
    ('a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'Complete Blood Count',        'CBC',     'Hematology',  350.00, 6,  'Blood',
     '{"Hemoglobin": {"min": 12, "max": 17, "unit": "g/dL"}, "WBC": {"min": 4000, "max": 11000, "unit": "cells/mcL"}}'),

    ('a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'Blood Sugar Fasting',         'BSF',     'Biochemistry', 80.00, 4,  'Blood',
     '{"Glucose": {"min": 70, "max": 100, "unit": "mg/dL"}}'),

    ('a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'Lipid Profile',               'LIPID',   'Biochemistry', 600.00, 12, 'Blood',
     '{"Total Cholesterol": {"min": 0, "max": 200, "unit": "mg/dL"}}'),

    ('a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'Thyroid Function Test',       'TFT',     'Endocrinology', 900.00, 24, 'Blood',
     '{"TSH": {"min": 0.4, "max": 4.0, "unit": "mIU/L"}}'),

    ('a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'Urine Routine & Microscopy',  'URM',     'Urine Analysis', 120.00, 4, 'Urine',
     '{"pH": {"min": 4.5, "max": 8.0, "unit": ""}}');