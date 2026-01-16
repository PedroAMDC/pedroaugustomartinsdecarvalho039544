CREATE TABLE artista_album (
    artista_id BIGINT NOT NULL REFERENCES artistas(id) ON DELETE CASCADE,
    album_id BIGINT NOT NULL REFERENCES albuns(id) ON DELETE CASCADE,
    PRIMARY KEY (artista_id, album_id)
);

CREATE INDEX idx_artista_album_artista ON artista_album(artista_id);
CREATE INDEX idx_artista_album_album ON artista_album(album_id);
