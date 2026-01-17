CREATE TABLE regionais (
    id INTEGER PRIMARY KEY,
    nome VARCHAR(200) NOT NULL,
    ativo BOOLEAN DEFAULT TRUE
);

CREATE INDEX idx_regionais_ativo ON regionais(ativo);
