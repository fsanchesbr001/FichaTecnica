-- fichatecnica.usuarios definição

CREATE TABLE `usuarios` (
                            `id` bigint NOT NULL AUTO_INCREMENT,
                            `login` varchar(255) NOT NULL,
                            `senha` varchar(255) NOT NULL,
                            `role` varchar(100) NOT NULL,
                            `data_alteracao` date NOT NULL,
                            `data_expiracao` date NOT NULL,
                            `tentativas` int NOT NULL,
                            `expirado` tinyint(1) NOT NULL,
                            `bloqueado` tinyint(1) NOT NULL,
                            PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Tabela com usuários';

INSERT INTO usuarios
(login, senha, `role`, data_alteracao, data_expiracao, tentativas, expirado, bloqueado)
VALUES('fsanchesbr001@gmail.com',
       '$2a$10$i42msHOOetd3HmoNwsbwXO3l.RVdpvXVqdvAY/VT3oZUhANMeBXkO',
       'ADMIN',
       '2025-03-01',
       '2025-06-01',
       0,
       0,
       0);

INSERT INTO usuarios
(login, senha, `role`, data_alteracao, data_expiracao, tentativas, expirado, bloqueado)
VALUES('fabricio@fabriciosanches.com',
       '$2a$10$i42msHOOetd3HmoNwsbwXO3l.RVdpvXVqdvAY/VT3oZUhANMeBXkO',
       'USER',
       '2025-03-01',
       '2025-06-01',
       0,
       0,
       0);

-- fichatecnica.unidade_medida definição

CREATE TABLE `unidade_medida` (
                                  `codigo` bigint NOT NULL AUTO_INCREMENT,
                                  `nome` varchar(255) NOT NULL,
                                  `sigla` varchar(10) NOT NULL,
                                  PRIMARY KEY (`codigo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Inserindo dados na tabela unidade_medida
INSERT INTO unidade_medida(codigo, nome, sigla) VALUES(1, 'Quilograma', 'Kg');
INSERT INTO unidade_medida(codigo, nome, sigla) VALUES(2, 'Grama', 'g');
INSERT INTO unidade_medida(codigo, nome, sigla) VALUES(3, 'Litro', 'l');
INSERT INTO unidade_medida(codigo, nome, sigla) VALUES(4, 'Mililitro', 'ml');
INSERT INTO unidade_medida(codigo, nome, sigla) VALUES(5, 'Quilometro', 'Km');
INSERT INTO unidade_medida(codigo, nome, sigla) VALUES(6, 'Metro', 'm');
INSERT INTO unidade_medida(codigo, nome, sigla) VALUES(7, 'Duzia', 'dz');
INSERT INTO unidade_medida(codigo, nome, sigla) VALUES(8, 'Unidade', 'un');

-- fichatecnica.conversao definição

CREATE TABLE `conversao` (
                             `codigo` bigint NOT NULL AUTO_INCREMENT,
                             `unidade_de` bigint NOT NULL,
                             `unidade_para` bigint NOT NULL,
                             `Operacao` varchar(15) NOT NULL,
                             `valor` decimal(10,2) NOT NULL,
                             PRIMARY KEY (`codigo`),
                             KEY `conversao_unidade_medida_FK` (`unidade_de`),
                             KEY `conversao_unidade_medida_FK_1` (`unidade_para`),
                             CONSTRAINT `conversao_unidade_medida_FK` FOREIGN KEY (`unidade_de`) REFERENCES `unidade_medida` (`codigo`) ON DELETE RESTRICT ON UPDATE RESTRICT,
                             CONSTRAINT `conversao_unidade_medida_FK_1` FOREIGN KEY (`unidade_para`) REFERENCES `unidade_medida` (`codigo`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Tabela com regras de Conversao';

-- Inserindo dados na tabela conversao
INSERT INTO conversao(codigo, unidade_de, unidade_para, Operacao, valor) VALUES(1, 1, 2, 'MULTIPLICA', 1000.00);
INSERT INTO conversao(codigo, unidade_de, unidade_para, Operacao, valor) VALUES(2, 2, 1, 'DIVIDE', 1000.00);
INSERT INTO conversao(codigo, unidade_de, unidade_para, Operacao, valor) VALUES(3, 3, 4, 'MULTIPLICA', 1000.00);
INSERT INTO conversao(codigo, unidade_de, unidade_para, Operacao, valor) VALUES(4, 4, 3, 'DIVIDE', 1000.00);
INSERT INTO conversao(codigo, unidade_de, unidade_para, Operacao, valor) VALUES(5, 5, 6, 'MULTIPLICA', 1000.00);
INSERT INTO conversao(codigo, unidade_de, unidade_para, Operacao, valor) VALUES(6, 6, 5, 'DIVIDE', 1000.00);
INSERT INTO conversao(codigo, unidade_de, unidade_para, Operacao, valor) VALUES(7, 7, 8, 'MULTIPLICA', 12.00);
INSERT INTO conversao(codigo, unidade_de, unidade_para, Operacao, valor) VALUES(8, 8, 7, 'DIVIDE', 12.00);

-- fichatecnica.produto definição

CREATE TABLE `produto` (
                           `codigo` bigint NOT NULL AUTO_INCREMENT COMMENT 'id do produto',
                           `nome` varchar(255) NOT NULL COMMENT 'Nome do produto',
                           `descricao` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'Descrição de produto',
                           `imagem` varchar(255) DEFAULT NULL COMMENT 'Endereço da imagem do prato',
                           `valor_venda` decimal(10,2) NOT NULL COMMENT 'Valor do produto',
                           `valor_itens` decimal(10,2) NOT NULL COMMENT 'Valor dos itens utilizados no produto',
                           PRIMARY KEY (`codigo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Lista de produtos';

-- fichatecnica.item definição

CREATE TABLE `item` (
                        `codigo` bigint NOT NULL AUTO_INCREMENT COMMENT 'Id do Item',
                        `nome` varchar(255) NOT NULL COMMENT 'Nome do Item',
                        `cd_unidade_medida` bigint NOT NULL COMMENT 'Unidade de medida',
                        `valor` decimal(10,2) NOT NULL COMMENT 'Valor do Item para a unidade de medida',
                        PRIMARY KEY (`codigo`),
                        KEY `item_unidade_medida_FK` (`cd_unidade_medida`),
                        CONSTRAINT `item_unidade_medida_FK` FOREIGN KEY (`cd_unidade_medida`) REFERENCES `unidade_medida` (`codigo`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Tabela de Itens';

-- fichatecnica.historico_item definição

CREATE TABLE `historico_item` (
                                  `codigo` bigint NOT NULL AUTO_INCREMENT COMMENT 'id do historico',
                                  `cd_item` bigint NOT NULL COMMENT 'Código do item',
                                  `valor` decimal(10,2) NOT NULL COMMENT 'Valor do Item no Histórico',
                                  `data_inicio` date NOT NULL COMMENT 'Data Inicio do preço',
                                  PRIMARY KEY (`codigo`),
                                  CONSTRAINT `historico_item_item_FK` FOREIGN KEY (`codigo`) REFERENCES `item` (`codigo`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Tabela com todas as atualizações de preços';

-- fichatecnica.item_produto definição

CREATE TABLE `item_produto` (
                                `cd_produto` bigint NOT NULL,
                                `cd_item` bigint NOT NULL,
                                `quantidade` decimal(10,2) NOT NULL,
                                `cd_unidade_para` bigint NOT NULL,
                                `valor` decimal(10,2) NOT NULL,
                                PRIMARY KEY (`cd_produto`, `cd_item`),
                                KEY `item_produto_produto_FK` (`cd_produto`),
                                KEY `item_produto_item_FK` (`cd_item`),
                                KEY `item_produto_unidade_medida_FK` (`cd_unidade_para`),
                                CONSTRAINT `item_produto_item_FK` FOREIGN KEY (`cd_item`) REFERENCES `item` (`codigo`),
                                CONSTRAINT `item_produto_produto_FK` FOREIGN KEY (`cd_produto`) REFERENCES `produto` (`codigo`),
                                CONSTRAINT `item_produto_unidade_medida_FK` FOREIGN KEY (`cd_unidade_para`) REFERENCES `unidade_medida` (`codigo`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Tabela com ligacao entre produto e itens.';
