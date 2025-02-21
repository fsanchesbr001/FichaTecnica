CREATE TABLE usuarios (
                                  id BIGINT auto_increment NOT NULL,
                                  login varchar(255) NOT NULL,
                                  senha varchar(255) NOT NULL,
                                  CONSTRAINT usuarios_pk PRIMARY KEY (id)
                                )
    ENGINE=InnoDB
    DEFAULT CHARSET=utf8mb4
    COLLATE=utf8mb4_0900_ai_ci
    COMMENT='Tabela com usuários';
