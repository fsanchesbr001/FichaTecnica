DELETE FROM conversao;
ALTER TABLE conversao AUTO_INCREMENT = 1;

DELETE FROM historico_item;
ALTER TABLE historico_item AUTO_INCREMENT = 1;

DELETE FROM item_produto;
ALTER TABLE item_produto AUTO_INCREMENT = 1;

DELETE FROM item;
ALTER TABLE item AUTO_INCREMENT = 1;

DELETE FROM unidade_medida;
ALTER TABLE unidade_medida AUTO_INCREMENT = 1;

DELETE FROM seguranca;
ALTER TABLE seguranca AUTO_INCREMENT = 1;

DELETE FROM usuarios;
ALTER TABLE usuarios AUTO_INCREMENT = 1;


DELETE FROM produto;
ALTER TABLE produto AUTO_INCREMENT = 1;

INSERT INTO unidade_medida(codigo, nome, sigla)
VALUES(1, 'Quilograma', 'Kg'),
      (2, 'Grama', 'g'),
      (3, 'Litro', 'l'),
      (4, 'Mililitro', 'ml'),
      (5, 'Quilometro', 'Km'),
      (6, 'Metro', 'm'),
      (7, 'Duzia', 'dz'),
      (8, 'Unidade', 'un');

INSERT INTO conversao(codigo, unidade_de, unidade_para, Operacao, valor)
VALUES(1, 2, 1, 'MULTIPLICA', 1000.00),
      (2, 1, 2, 'DIVIDE', 1000.00),
      (3, 2, 2, 'MULTIPLICA', 1.00),
      (4, 1, 1, 'MULTIPLICA', 1.00),
      (5, 3, 4, 'DIVIDE', 1000.00),
      (6, 4, 3, 'MULTIPLICA', 1000.00),
      (7, 3, 3, 'MULTIPLICA', 1.00),
      (8, 4, 4, 'MULTIPLICA', 1.00),
      (9, 6, 5, 'MULTIPLICA', 1000.00),
      (10, 5, 6, 'DIVIDE', 1000.00),
      (11, 6, 6, 'MULTIPLICA', 1.00),
      (12, 5, 5, 'MULTIPLICA', 1.00),
      (13, 8, 7, 'MULTIPLICA', 12.00),
      (14, 7, 8, 'DIVIDE', 12.00),
      (15, 7, 7, 'MULTIPLICA', 1.00),
      (16, 8, 8, 'MULTIPLICA', 1.00);

INSERT INTO usuarios(id, login, senha, nome, `role`)
VALUES(1, 'fsanchesbr001@gmail.com', '$2a$10$WiA4M3.UnzUkC5F06WX3reFszowLXYYBKWc8EZBRUu6aOGWcTtV.i', 'Fabricio Sanches', 'ADMIN'),
      (2, 'contato@fabriciosanches.com', '$2a$10$7cIHlMQduCKLddwWhJFLXu7CN4.CZ1C.OvDdCp9uQ9CCVHYxGjp72', 'Sistema Ollivander', 'SYSTEM');

INSERT INTO seguranca(codigo, email, data_criacao, data_expiracao_senha, qtde_tentativas_login, token_seguranca, data_expiracao_token, cpf, bloqueio_adm, bloqueio_tentativas, bloqueio_expiracao, primeiro_acesso)
VALUES(1, 'fsanchesbr001@gmail.com', '2025-06-11 22:10:56', '2056-12-13 07:02:00', 5, NULL, NULL, '81127260197', 0, 0, 0, 0),
      (2, 'contato@fabriciosanches.com', '2026-03-19 03:29:08', '2056-03-19 05:29:08', 5, NULL, NULL, '81127260197', 0, 0, 0, 0);

