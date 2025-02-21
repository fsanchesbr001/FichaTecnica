CREATE TABLE fichatecnica.Conversao (
                                        codigo BIGINT auto_increment NOT NULL,
                                        unidade_de INT(5) NOT NULL,
                                        unidade_para INT(5) NOT NULL,
                                        Operacao VARCHAR(15) NOT NULL,
                                        valor DECIMAL(10,2) NOT NULL,
                                        CONSTRAINT Conversao_pk PRIMARY KEY (codigo)
)
    ENGINE=InnoDB
    DEFAULT CHARSET=utf8mb4
    COLLATE=utf8mb4_0900_ai_ci
    COMMENT='Tabela com regras de Conversao';
