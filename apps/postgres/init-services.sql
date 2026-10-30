-- Runs after smart_home/init.sql. One PostgreSQL for local development, but every
-- microservice owns a separate database and cannot connect to the others.

CREATE ROLE heating LOGIN PASSWORD 'heating';
CREATE DATABASE devices OWNER heating;

CREATE ROLE telemetry LOGIN PASSWORD 'telemetry';
CREATE DATABASE telemetry OWNER telemetry;

REVOKE CONNECT ON DATABASE smarthome FROM PUBLIC;
REVOKE CONNECT ON DATABASE devices FROM PUBLIC;
REVOKE CONNECT ON DATABASE telemetry FROM PUBLIC;

-- Creating the extension requires a superuser; the service only creates hypertables
\c telemetry
CREATE EXTENSION IF NOT EXISTS timescaledb;
