-- fichatecnica.usuarios definição

CREATE TABLE `usuarios` (
                            `id` bigint NOT NULL AUTO_INCREMENT,
                            `login` varchar(255) NOT NULL,
                            `senha` varchar(255) NULL,
                            `nome` varchar(255) NULL,
                            `role` varchar(100) NULL,
                            PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Tabela com usuários';

INSERT INTO usuarios
(login, senha, role, nome)
VALUES('fsanchesbr001@gmail.com',
       '$2a$10$i42msHOOetd3HmoNwsbwXO3l.RVdpvXVqdvAY/VT3oZUhANMeBXkO',
       'ADMIN',
       'Fabricio Sanches');

INSERT INTO usuarios
(login, senha, role, nome)
VALUES('fabricio@fabriciosanches.com',
       '$2a$10$i42msHOOetd3HmoNwsbwXO3l.RVdpvXVqdvAY/VT3oZUhANMeBXkO',
       'USER',
       'Fabricio Sanches 2');

-- fichatecnica.seguranca definição
CREATE TABLE fichatecnica.seguranca (
                                        codigo BIGINT auto_increment NOT NULL,
                                        email varchar(255) NOT NULL,
                                        data_criacao DATETIME NULL,
                                        data_expiracao_senha DATETIME NULL,
                                        qtde_tentativas_login DECIMAL(1) NULL,
                                        token_seguranca VARCHAR(8) NULL,
                                        data_expiracao_token DATETIME NULL,
                                        cpf VARCHAR(11) NOT NULL,
                                        bloqueio_adm BOOL DEFAULT FALSE NOT NULL,
                                        bloqueio_tentativas BOOL DEFAULT FALSE NOT NULL,
                                        bloqueio_expiracao BOOL DEFAULT FALSE NOT NULL,
                                        primeiro_acesso BOOL DEFAULT FALSE NOT NULL,
                                        PRIMARY KEY (codigo)
)
    ENGINE=InnoDB
    DEFAULT CHARSET=utf8mb4
    COLLATE=utf8mb4_0900_ai_ci
    COMMENT='Informacoes de controle de acesso.';

-- Inserindo dados na tabela seguranca
INSERT INTO fichatecnica.seguranca (
    email,
    data_criacao,
    data_expiracao_senha,
    qtde_tentativas_login,
    token_seguranca,
    data_expiracao_token,
    cpf,
    bloqueio_adm,
    bloqueio_tentativas,
    bloqueio_expiracao,
    primeiro_acesso
) VALUES (
             'fsanchesbr001@gmail.com',
             SYSDATE(),
             DATE_ADD(SYSDATE(), INTERVAL 3 MONTH),
             5,
             'ABCDEFGH',
             DATE_ADD(SYSDATE(), INTERVAL 2 HOUR),
             '81127260197',
             FALSE,
             FALSE,
             FALSE,
             FALSE
         );

INSERT INTO fichatecnica.seguranca (
    email,
    data_criacao,
    data_expiracao_senha,
    qtde_tentativas_login,
    token_seguranca,
    data_expiracao_token,
    cpf,
    bloqueio_adm,
    bloqueio_tentativas,
    bloqueio_expiracao,
    primeiro_acesso
) VALUES (
             'fabricio@fabriciosanches.com',
             SYSDATE(),
             DATE_ADD(SYSDATE(), INTERVAL 3 MONTH),
             5,
             'ABCDEFGH',
             DATE_ADD(SYSDATE(), INTERVAL 2 HOUR),
             '81127260197',
             FALSE,
             FALSE,
             FALSE,
             FALSE
         );

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
INSERT INTO conversao(codigo, unidade_de, unidade_para, Operacao, valor) VALUES(1, 1, 2, 'DIVIDE', 1000.00);
INSERT INTO conversao(codigo, unidade_de, unidade_para, Operacao, valor) VALUES(2, 2, 1, 'MULTIPLICA', 1000.00);
INSERT INTO conversao(codigo, unidade_de, unidade_para, Operacao, valor) VALUES(3, 3, 4, 'DIVIDE', 1000.00);
INSERT INTO conversao(codigo, unidade_de, unidade_para, Operacao, valor) VALUES(4, 4, 3, 'MULTIPLICA', 1000.00);
INSERT INTO conversao(codigo, unidade_de, unidade_para, Operacao, valor) VALUES(5, 5, 6, 'DIVIDE', 1000.00);
INSERT INTO conversao(codigo, unidade_de, unidade_para, Operacao, valor) VALUES(6, 6, 5, 'MULTIPLICA', 1000.00);
INSERT INTO conversao(codigo, unidade_de, unidade_para, Operacao, valor) VALUES(7, 7, 8, 'DIVIDE', 12.00);
INSERT INTO conversao(codigo, unidade_de, unidade_para, Operacao, valor) VALUES(8, 8, 7, 'MULTIPLICA', 12.00);
INSERT INTO conversao(codigo, unidade_de, unidade_para, Operacao, valor) VALUES(9, 1, 1, 'MULTIPLICA', 1.00);
INSERT INTO conversao(codigo, unidade_de, unidade_para, Operacao, valor) VALUES(10, 2, 2, 'MULTIPLICA', 1.00);
INSERT INTO conversao(codigo, unidade_de, unidade_para, Operacao, valor) VALUES(11, 3, 3, 'MULTIPLICA', 1.00);
INSERT INTO conversao(codigo, unidade_de, unidade_para, Operacao, valor) VALUES(12, 4, 4, 'MULTIPLICA', 1.00);
INSERT INTO conversao(codigo, unidade_de, unidade_para, Operacao, valor) VALUES(13, 5, 5, 'MULTIPLICA', 1.00);
INSERT INTO conversao(codigo, unidade_de, unidade_para, Operacao, valor) VALUES(14, 6, 6, 'MULTIPLICA', 1.00);
INSERT INTO conversao(codigo, unidade_de, unidade_para, Operacao, valor) VALUES(15, 7, 7, 'MULTIPLICA', 1.00);
INSERT INTO conversao(codigo, unidade_de, unidade_para, Operacao, valor) VALUES(16, 8, 8, 'MULTIPLICA', 1.00);


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
                                  KEY `historico_item_item_FK` (`cd_item`),
                                  CONSTRAINT `historico_item_item_FK` FOREIGN KEY (`cd_item`) REFERENCES `item` (`codigo`) ON DELETE RESTRICT ON UPDATE RESTRICT
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
