CREATE TABLE artistas (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('CANTOR', 'BANDA')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_artistas_nome ON artistas(nome);
CREATE INDEX idx_artistas_tipo ON artistas(tipo);

CREATE TRIGGER update_artistas_updated_at
    BEFORE UPDATE ON artistas
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
