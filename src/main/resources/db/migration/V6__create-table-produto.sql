CREATE TABLE fichatecnica.produto (
                                      codigo BIGINT auto_increment NOT NULL COMMENT 'id do produto',
                                      nome VARCHAR(255) NOT NULL COMMENT 'Nome do produto',
                                      descricao VARCHAR(255) NULL COMMENT 'Descrição de produto',
                                      imagem VARCHAR(255) NULL COMMENT 'Endereço da imagem do prato',
                                      valor NUMERIC(10,2) NULL COMMENT 'Valor do produto',
                                      CONSTRAINT produto_pk PRIMARY KEY (codigo)
)
    ENGINE=InnoDB
    DEFAULT CHARSET=utf8mb4
    COLLATE=utf8mb4_0900_ai_ci
    COMMENT='Lista de produtos';
