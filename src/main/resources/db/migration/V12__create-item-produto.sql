CREATE TABLE fichatecnica.item_pedido (
                                          codigo BIGINT auto_increment NOT NULL,
                                          cd_produto BIGINT NOT NULL,
                                          cd_item BIGINT NOT NULL,
                                          quantidade DECIMAL(10) NOT NULL,
                                          cd_unidade_para BIGINT NOT NULL,
                                          valor DECIMAL(10,2) NOT NULL,
                                          CONSTRAINT item_pedido_conversao_FK FOREIGN KEY (codigo) REFERENCES fichatecnica.conversao(codigo)
)
    ENGINE=InnoDB
    DEFAULT CHARSET=utf8mb4
    COLLATE=utf8mb4_0900_ai_ci
    COMMENT='Tabela com ligacao entre produto e itens.';
