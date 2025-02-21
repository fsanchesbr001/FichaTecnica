CREATE TABLE fichatecnica.item (
                                   codigo BIGINT auto_increment NOT NULL COMMENT 'Id do Item',
                                   nome varchar(255) NOT NULL COMMENT 'Nome do Item',
                                   cd_unidade_medida BIGINT NOT NULL COMMENT 'Unidade de medida',
                                   valor DECIMAL(10,2) NOT NULL COMMENT 'Valor do Item para a unidade de medida',
                                   CONSTRAINT Item_pk PRIMARY KEY (codigo)
)
    ENGINE=InnoDB
    DEFAULT CHARSET=utf8mb4
    COLLATE=utf8mb4_0900_ai_ci
    COMMENT='Tabela de Itens';
