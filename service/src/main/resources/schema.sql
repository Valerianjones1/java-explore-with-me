DROP TABLE IF EXISTS users, categories,requests, events, locations, compilations, event_compilations;

CREATE TABLE IF NOT EXISTS users
(
    id    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email varchar(254) UNIQUE NOT NULL,
    name  varchar(250) NOT NULL
);

CREATE TABLE IF NOT EXISTS categories
(
    id    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name  varchar(250) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS locations
(
    id    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    latitude REAL,
    longitude REAL
);

CREATE TABLE IF NOT EXISTS compilations
(
    id    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    is_pinned BOOLEAN,
    title VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS events
(
    id    BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    annotation  VARCHAR(2000) NOT NULL,
    category_id    BIGINT,
    description  VARCHAR(7000) NOT NULL,
    event_date  TIMESTAMP WITHOUT TIME ZONE,
    location_id    BIGINT,
    is_paid BOOLEAN,
    request_moderation BOOLEAN,
    initiator_id BIGINT,
    participant_limit BIGINT,
    compilation_id BIGINT,
    state VARCHAR(100),
    title  VARCHAR(120) NOT NULL,
    date_create TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT fk_events_to_categories FOREIGN KEY (category_id) references categories (id),
    CONSTRAINT fk_events_to_locations FOREIGN KEY (location_id) references locations (id),
    CONSTRAINT fk_events_to_users FOREIGN KEY (initiator_id) references users (id),
    CONSTRAINT fk_events_to_compilations FOREIGN KEY (compilation_id) references compilations (id)
);

CREATE TABLE IF NOT EXISTS event_compilations (
  event_id BIGINT NOT NULL REFERENCES events(id),
  compilation_id BIGINT NOT NULL REFERENCES compilations(id),
  PRIMARY KEY (event_id, compilation_id)
);

CREATE TABLE IF NOT EXISTS requests
(
    id    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    created  TIMESTAMP WITHOUT TIME ZONE,
    requester_id  BIGINT,
    event_id    BIGINT,
    status VARCHAR(100),
    CONSTRAINT fk_requests_to_users FOREIGN KEY (requester_id) references users (id),
    CONSTRAINT fk_requests_to_events FOREIGN KEY (event_id) references events (id),
    UNIQUE(requester_id, event_id)
);