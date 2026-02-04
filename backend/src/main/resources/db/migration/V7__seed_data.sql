INSERT INTO usuarios (email, password_hash, nome, ativo)
VALUES ('admin@artistas.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Administrador', TRUE);

INSERT INTO artistas (nome, tipo) VALUES
('Serj Tankian', 'CANTOR'),
('Mike Shinoda', 'CANTOR'),
('Michel Telo', 'CANTOR'),
('Guns N'' Roses', 'BANDA');

INSERT INTO albuns (titulo, ano_lancamento) VALUES
('Harakiri', 2012),
('Black Blooms', 2019),
('The Rough Dog', 2011),
('The Rising Tied', 2005),
('Post Traumatic', 2018),
('Post Traumatic EP', 2018),
('Where''d You Go', 2006),
('Bem Sertanejo', 2014),
('Bem Sertanejo - O Show (Ao Vivo)', 2015),
('Bem Sertanejo - (1a Temporada) - EP', 2014),
('Use Your Illusion I', 1991),
('Use Your Illusion II', 1991),
('Greatest Hits', 2004);

INSERT INTO artista_album (artista_id, album_id) VALUES
(1, 1), (1, 2), (1, 3),
(2, 4), (2, 5), (2, 6), (2, 7),
(3, 8), (3, 9), (3, 10),
(4, 11), (4, 12), (4, 13);
