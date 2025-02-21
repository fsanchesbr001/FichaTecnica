CREATE TABLE fichatecnica.historico_produto (
                                                codigo BIGINT auto_increment NOT NULL COMMENT 'codigo historico produto',
                                                cd_produto BIGINT NOT NULL COMMENT 'id do produto',
                                                valor NUMERIC(10,2) NOT NULL,
                                                data_inicio DATE NOT NULL,
                                                data_fim DATE NULL,
                                                CONSTRAINT historico_produto_pk PRIMARY KEY (codigo)
)
    ENGINE=InnoDB
    DEFAULT CHARSET=utf8mb4
    COLLATE=utf8mb4_0900_ai_ci
    COMMENT='Historico de preços dos produtos';
