CREATE TABLE fichatecnica.historico_precos (
                                               codigo BIGINT auto_increment NOT NULL COMMENT 'id do historico',
                                               cd_item BIGINT NOT NULL COMMENT 'Código do item',
                                               valor DECIMAL(10,2) NOT NULL COMMENT 'Valor do Item no Histórico',
                                               data_inicio DATE NOT NULL COMMENT 'Data Inicio do preço',
                                               data_fim DATE NULL COMMENT 'Data fim do preço',
                                               CONSTRAINT historico_precos_pk PRIMARY KEY (codigo),
                                               CONSTRAINT historico_precos_item_FK FOREIGN KEY (codigo) REFERENCES fichatecnica.item(codigo)
)
    ENGINE=InnoDB
    DEFAULT CHARSET=utf8mb4
    COLLATE=utf8mb4_0900_ai_ci
    COMMENT='Tabela com todas as atualizações de preços';
