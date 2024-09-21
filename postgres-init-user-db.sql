CREATE USER keycloak WITH PASSWORD 'keycloak';

CREATE DATABASE keycloak;
GRANT ALL PRIVILEGES ON DATABASE keycloak TO keycloak;
\c keycloak
CREATE SCHEMA keycloak AUTHORIZATION keycloak;
GRANT ALL ON SCHEMA keycloak TO keycloak;
ALTER USER keycloak SET SEARCH_PATH = 'keycloak';
