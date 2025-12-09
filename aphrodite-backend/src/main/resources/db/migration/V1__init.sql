CREATE TABLE access_log_t (
    id_c          VARCHAR(36) PRIMARY KEY,
    remote_addr_c VARCHAR(15),
    time_c        BIGINT NOT NULL,
    headers_c     TEXT,
    cookies_c     TEXT,
    method_c      VARCHAR(10),
    request_uri_c VARCHAR(255),
    params_c      TEXT);

CREATE TABLE namespace_t (
    id_c         VARCHAR(36) PRIMARY KEY,
    name_c       VARCHAR(128) NOT NULL UNIQUE,
    created_at_c BIGINT       NOT NULL);

CREATE TABLE encryption_keys_t (
    id_c                 VARCHAR(36) PRIMARY KEY,
    hash_c               VARCHAR(128) NOT NULL UNIQUE,
    encryptor_hash_c     VARCHAR(128) NOT NULL,
    encrypted_key_c      BYTEA        NOT NULL,
    encrypted_password_c BYTEA,
    created_at_c         BIGINT       NOT NULL,
    namespace_id_c       VARCHAR(36)  NOT NULL,
    FOREIGN KEY (namespace_id_c) REFERENCES namespace_t (id_c)
        ON DELETE CASCADE
        ON UPDATE CASCADE
        DEFERRABLE INITIALLY DEFERRED);

CREATE TABLE application_properties_t (
    key_c             TEXT PRIMARY KEY,
    encrypted_value_c BYTEA,
    created_at_c      BIGINT       NOT NULL,
    version_c         INTEGER      NOT NULL DEFAULT 1,
    encryptor_hash_c  VARCHAR(128) NOT NULL,
    namespace_id_c    VARCHAR(36)  NOT NULL,
    FOREIGN KEY (encryptor_hash_c) REFERENCES encryption_keys_t (hash_c),
    FOREIGN KEY (namespace_id_c) REFERENCES namespace_t (id_c)
        ON DELETE CASCADE
        ON UPDATE CASCADE
        DEFERRABLE INITIALLY DEFERRED,
    CONSTRAINT uq_application_properties UNIQUE (key_c, version_c, namespace_id_c));

CREATE TABLE privileges_t (
    id_c           VARCHAR(36) PRIMARY KEY,
    name_c         VARCHAR(128) NOT NULL,
    description_c  TEXT,
    created_at_c   BIGINT       NOT NULL,
    namespace_id_c VARCHAR(36)  NOT NULL,
    FOREIGN KEY (namespace_id_c) REFERENCES namespace_t (id_c)
        ON DELETE CASCADE
        ON UPDATE CASCADE
        DEFERRABLE INITIALLY DEFERRED,
    CONSTRAINT uq_privileges UNIQUE (name_c, namespace_id_c));

CREATE TABLE roles_t (
    id_c                            VARCHAR(36) PRIMARY KEY,
    name_c                          VARCHAR(128) NOT NULL,
    description_c                   TEXT,
    created_at_c                    BIGINT       NOT NULL,
    namespace_id_c                  VARCHAR(36)  NOT NULL,
    role_priv_uri_c                 VARYING(255),
    role_priv_uri_allowed_methods_c VARYING(255),
    role_priv_uri_denied_methods_c  VARYING(255),
    FOREIGN KEY (namespace_id_c) REFERENCES namespace_t (id_c)
        ON DELETE CASCADE
        ON UPDATE CASCADE
        DEFERRABLE INITIALLY DEFERRED,
    CONSTRAINT uq_roles UNIQUE (name_c, namespace_id_c));

CREATE TABLE users_t (
    id_c                    VARCHAR(36) PRIMARY KEY,
    username_c              VARCHAR(128) NOT NULL,
    email_c                 VARCHAR(128),
    password_hash_c         VARCHAR(255),
    password_last_changed_c BIGINT,
    expired_c               BOOLEAN DEFAULT true,
    locked_c                BOOLEAN DEFAULT true,
    enabled_c               BOOLEAN DEFAULT false,
    namespace_id_c          VARCHAR(36)  NOT NULL,
    FOREIGN KEY (namespace_id_c) REFERENCES namespace_t (id_c)
        ON DELETE CASCADE
        ON UPDATE CASCADE
        DEFERRABLE INITIALLY DEFERRED,
    CONSTRAINT uq_users_username UNIQUE (username_c, namespace_id_c),
    CONSTRAINT uq_users_email UNIQUE (email_c, namespace_id_c));

CREATE TABLE roles_to_privileges_t (
    role_id_c      VARCHAR(36) NOT NULL,
    privilege_id_c VARCHAR(36) NOT NULL,
    FOREIGN KEY (role_id_c) REFERENCES roles_t (id_c)
        ON DELETE CASCADE
        ON UPDATE CASCADE
        DEFERRABLE INITIALLY DEFERRED,
    FOREIGN KEY (privilege_id_c) REFERENCES privileges_t (id_c)
        ON DELETE CASCADE
        ON UPDATE CASCADE
        DEFERRABLE INITIALLY DEFERRED,
    CONSTRAINT pk_roles_to_privileges PRIMARY KEY (role_id_c, privilege_id_c));

CREATE TABLE users_to_roles_t (
    role_id_c VARCHAR(36) NOT NULL,
    user_id_c VARCHAR(36) NOT NULL,
    FOREIGN KEY (role_id_c) REFERENCES roles_t (id_c)
        ON DELETE CASCADE
        ON UPDATE CASCADE
        DEFERRABLE INITIALLY DEFERRED,
    FOREIGN KEY (user_id_c) REFERENCES users_t (id_c)
        ON DELETE CASCADE
        ON UPDATE CASCADE
        DEFERRABLE INITIALLY DEFERRED,
    CONSTRAINT pk_users_to_roles PRIMARY KEY (role_id_c, user_id_c));

CREATE TABLE api_keys_t (
    id_c           VARCHAR(36) PRIMARY KEY,
    created_at_c   BIGINT      NOT NULL,
    last_used_at_c BIGINT,
    expire_at_c    BIGINT      NOT NULL,
    user_id_c      VARCHAR(36) NOT NULL,
    comment_c      TEXT,
    origin_ip_c    VARCHAR(15),
    is_active_c    BOOLEAN DEFAULT true,
    namespace_id_c VARCHAR(36) NOT NULL,
    FOREIGN KEY (user_id_c) REFERENCES users_t (id_c)
        ON DELETE CASCADE
        ON UPDATE CASCADE
        DEFERRABLE INITIALLY DEFERRED,
    FOREIGN KEY (namespace_id_c) REFERENCES namespace_t (id_c)
        ON DELETE CASCADE
        ON UPDATE CASCADE
        DEFERRABLE INITIALLY DEFERRED);

CREATE TABLE buckets_t (
    id_c           VARCHAR(36) PRIMARY KEY,
    name_c         VARCHAR(128) NOT NULL,
    namespace_id_c VARCHAR(36)  NOT NULL,
    created_at_c   BIGINT       NOT NULL,
    FOREIGN KEY (namespace_id_c) REFERENCES namespace_t (id_c)
        ON DELETE CASCADE
        ON UPDATE CASCADE
        DEFERRABLE INITIALLY DEFERRED,
    CONSTRAINT uq_buckets UNIQUE (name_c, namespace_id_c));

CREATE TABLE key_value_t (
    id_c              VARCHAR(36) PRIMARY KEY,
    key_c             TEXT         NOT NULL,
    encrypted_value_c BYTEA        NOT NULL,
    deleted_c         BOOLEAN      NOT NULL DEFAULT false,
    version_c         INTEGER      NOT NULL,
    created_at_c      BIGINT       NOT NULL,
    encryptor_hash_c  VARCHAR(128) NOT NULL,
    bucket_id_c       VARCHAR(36)  NOT NULL,
    FOREIGN KEY (bucket_id_c) REFERENCES buckets_t (id_c)
        ON DELETE CASCADE
        ON UPDATE CASCADE
        DEFERRABLE INITIALLY DEFERRED,
    FOREIGN KEY (encryptor_hash_c) REFERENCES encryption_keys_t (hash_c)
        ON DELETE CASCADE
        ON UPDATE CASCADE
        DEFERRABLE INITIALLY DEFERRED,
    CONSTRAINT uq_key_version UNIQUE (key_c, version_c, bucket_id_c));

CREATE TABLE terraform_state_lock_t (
    id_c        VARCHAR(128) PRIMARY KEY,
    operation_c VARCHAR(128) NOT NULL,
    info_c      TEXT         NOT NULL,
    who_c       VARCHAR(128) NOT NULL,
    version_c   VARCHAR(128) NOT NULL,
    created_c   VARCHAR(128) NOT NULL,
    path_c      VARCHAR(128) NOT NULL);

CREATE TABLE terraform_state_t (
    id_c              VARCHAR(36) PRIMARY KEY,
    name_c            VARCHAR(256) NOT NULL,
    encrypted_state_c BYTEA,
    created_at_c      BIGINT       NOT NULL,
    encryptor_hash_c  VARCHAR(128) NOT NULL,
    deleted_c         BOOLEAN      NOT NULL DEFAULT false,
    version_c         INTEGER      NOT NULL,
    bucket_id_c       VARCHAR(36)  NOT NULL,
    lock_id_c         VARCHAR(36),
    UNIQUE (name_c, bucket_id_c, version_c),
    FOREIGN KEY (bucket_id_c) REFERENCES buckets_t (id_c)
        ON DELETE CASCADE
        ON UPDATE CASCADE
        DEFERRABLE INITIALLY DEFERRED,
    FOREIGN KEY (encryptor_hash_c) REFERENCES encryption_keys_t (hash_c)
        ON DELETE CASCADE
        ON UPDATE CASCADE
        DEFERRABLE INITIALLY DEFERRED,
    FOREIGN KEY (lock_id_c) REFERENCES terraform_state_lock_t (id_c)
        ON DELETE SET NULL
        ON UPDATE CASCADE
        DEFERRABLE INITIALLY DEFERRED);