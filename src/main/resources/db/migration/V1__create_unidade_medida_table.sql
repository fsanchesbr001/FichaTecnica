-- Criação do banco de dados FichaTecnica
CREATE DATABASE IF NOT EXISTS FichaTecnica;

-- Uso do banco de dados FichaTecnica
USE FichaTecnica;

-- Criação da tabela unidade_medida
CREATE TABLE IF NOT EXISTS unidade_medida (
                                              codigo INT AUTO_INCREMENT PRIMARY KEY,
                                              nome VARCHAR(255) NOT NULL,
                                              sigla VARCHAR(10) NOT NULL
);
