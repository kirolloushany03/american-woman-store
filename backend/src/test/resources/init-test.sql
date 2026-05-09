-- Test database initialization script for Testcontainers
-- This runs once when the MySQL container starts

CREATE DATABASE IF NOT EXISTS awstore;
USE awstore;

-- Note: Tables will be created by Hibernate ddl-auto=create-drop
-- This script is mainly for any manual setup if needed

