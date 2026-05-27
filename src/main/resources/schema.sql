CREATE TABLE IF NOT EXISTS pets (
    id          VARCHAR(36) PRIMARY KEY,
    name        VARCHAR(255),
    species     VARCHAR(255),
    breed       VARCHAR(255),
    age         INT,
    price       DOUBLE
);
