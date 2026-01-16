CREATE TABLE albuns (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    ano_lancamento INTEGER NOT NULL CHECK (ano_lancamento >= 1900 AND ano_lancamento <= 2100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_albuns_titulo ON albuns(titulo);
CREATE INDEX idx_albuns_ano ON albuns(ano_lancamento);

CREATE TRIGGER update_albuns_updated_at
    BEFORE UPDATE ON albuns
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
