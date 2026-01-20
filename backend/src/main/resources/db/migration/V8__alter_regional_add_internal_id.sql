ALTER TABLE regionais RENAME COLUMN id TO external_id;

ALTER TABLE regionais ADD COLUMN id BIGSERIAL;

ALTER TABLE regionais DROP CONSTRAINT IF EXISTS regionais_pkey;

ALTER TABLE regionais ADD PRIMARY KEY (id);

CREATE INDEX idx_regionais_external_id ON regionais(external_id);

CREATE INDEX idx_regionais_external_id_ativo ON regionais(external_id, ativo);
