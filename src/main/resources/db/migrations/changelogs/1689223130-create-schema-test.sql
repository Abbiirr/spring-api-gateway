-- liquibase formatted sql

-- changeset Noman5237:1689223130l-1
create
    extension if not exists "uuid-ossp";

-- changeset Noman5237:1689223130-2
-- create a schema for the application
create schema if not exists "tax";
