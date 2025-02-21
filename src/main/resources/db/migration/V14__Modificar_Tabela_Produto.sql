ALTER TABLE fichatecnica.produto MODIFY COLUMN valor decimal(10,2) NOT NULL COMMENT 'Valor do produto';
ALTER TABLE fichatecnica.produto MODIFY COLUMN descricao varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'Descrição de produto';
