ALTER TABLE fichatecnica.seguranca MODIFY COLUMN token_seguranca varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL;
ALTER TABLE fichatecnica.seguranca MODIFY COLUMN data_expiracao_token datetime NULL;
