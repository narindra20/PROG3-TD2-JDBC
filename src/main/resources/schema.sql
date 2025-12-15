CREATE TABLE Team (
     id SERIAL PRIMARY KEY,
     name VARCHAR(100) NOT NULL,
     continent continent_type NOT NULL
);

CREATE TABLE Player (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    age INT CHECK (age > 0),
    position player_position NOT NULL,
    id_team INT,
    CONSTRAINT fk_team
      FOREIGN KEY (id_team)
        REFERENCES Team(id)
    ON DELETE SET NULL
);

