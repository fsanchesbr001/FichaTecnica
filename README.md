# Projeto Ficha Técnica
## Descrição
Este projeto visa a criação de uma ficha técnica de um produto, onde é possível adicionar, editar e excluir produtos, além de visualizar a lista de produtos cadastrados.
## Tecnologias
- Java 17
- Spring Boot 3.4.1

## Dependências
- Spring Data JPA

## Docker (Backend + MySQL)

Arquivos adicionados na raiz do projeto:
- `Dockerfile`
- `docker-compose.yml`
- `docker-compose.prod.yml`
- `.env.example`
- `.env.prod.example`
- `.dockerignore`

### Preparar variáveis locais

```bash
cp .env.example .env
```

### Subir ambiente de desenvolvimento

```bash
docker compose --env-file .env up --build -d
```

### Subir ambiente com perfil de produção

```bash
docker compose --env-file .env.prod -f docker-compose.yml -f docker-compose.prod.yml up --build -d
```

### Ver logs do backend

```bash
docker compose --env-file .env logs -f backend
```

### Parar os containers

```bash
docker compose --env-file .env down
```

### Sem credenciais fixas no compose

As credenciais agora ficam em `.env` (ignorado no git), e o arquivo versionado
`.env.example` serve apenas como modelo.

### Docker secrets para produção

O arquivo `docker-compose.prod.yml` suporta segredos via arquivos montados em
`/run/secrets`.

Crie arquivos reais (não versionados) com base nos exemplos em `docker/secrets/*.txt.example`:

```bash
cp docker/secrets/mysql_password.txt.example docker/secrets/mysql_password.txt
cp docker/secrets/mysql_root_password.txt.example docker/secrets/mysql_root_password.txt
cp docker/secrets/jwt_secret.txt.example docker/secrets/jwt_secret.txt
cp docker/secrets/mail_pwd.txt.example docker/secrets/mail_pwd.txt
cp docker/secrets/system_pwd.txt.example docker/secrets/system_pwd.txt
cp .env.prod.example .env.prod
```

Depois, ajuste os valores dos arquivos `*.txt` e da `.env.prod`.

### Variáveis principais

- `APP_PROFILE=development` (ou `production` no compose de produção)
- `DB_IP=mysql`
- `DB_PORT=3306`
- `DB_NAME=${MYSQL_DATABASE}`
- `DB_USER=${MYSQL_USER}`
- `DB_PWD=${MYSQL_PASSWORD}`

## Swagger / OpenAPI

A documentação automática da API fica disponível em:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- JSON OpenAPI: `http://localhost:8080/v3/api-docs`
- Atalho amigável: `http://localhost:8080/swagger` ou `http://localhost:8080/docs`

Se o backend estiver em outro host/porta, ajuste a URL conforme o ambiente.

## Documentação
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Java 17](https://docs.oracle.com/en/java/javase/17/)
- [Maven](https://maven.apache.org/)

## Próximos Passos

- Adicionar Observabilidade e monitoramento da aplicação.
- Adicionar Documentação da API com Swagger.


## Controle de Versão
- 02/03/2025 - Refatoração Completa
- 02/03/2025 - Criação do Banco de Dados
- 02/03/2025 - Versão 1.1 - Estável sem Itens de Produto
- 02/03/2025 - Versão 1.2 - Estável com Segurança de Usuário implementada
- 03/03/2025 - Versão 1.2.1 - Estável com início de Itens 
- 04/03/2025 - Versão 1.3.0 - Estável com Itens e Histórico de Itens 
- 07/03/2025 - Versão 1.3.1 - Estável com as Listagens e deleções de produtos
- 14/03/2025 - Versão 1.4.0 - Estável com refatoração de DTOs
- 22/03/2025 - Versão 1.4.1 - Estável com Produto Ok
- 23/03/2025 - Versão 1.5.0 - Estável e salvando ItensProduto
- 25/03/2025 - Versão 1.5.1 - Estável com ItensProduto Ok e totais de Itens 
- 27/03/2025 - Versão 1.5.2 - Estável com Lista de Produtos por itens Ok
- 29/03/2025 - Versão 1.5.3 - Estável com Conversão de valores
- 30/03/2025 - Versão 1.5.4 - Estável com atualização de valor dos Itens OK e deleção de ItensProduto OK
- 03/04/2025 - Versão 1.5.6 - Correção de referências Circulares em HistoricoItem
- 05/04/2025 - Versão 1.5.7 - Correção de chave estrangeira e limpeza de codigo em HistoricoItem
- 09/04/2025 - Versão 1.5.8 - Versão Estável com alterar ‘Item’ OK
- 10/04/2025 - Versão 1.5.9 - Versão Estável com deletar “item” OK
- 24/05/2025 - Versão 1.6.0 - Criação e populacao da tabela seguranca
- 29/05/2025 - Versão 1.6.1 - Envio de endereço eletrónico de confirmação de ‘token’ de segurança
- 04/06/2025 - Versão 1.6.2 - Formatação de data e hora na saida do envio do endereço eletrónico
- 06/06/2025 - Versão 1.6.3 - Troca de senha e “login” funcionando
- 07/06/2025 - Versão 1.6.4 - Set e Checagem de Bloqueios e checagens pré-login, tratando tentativas de “login”
- 12/06/2025 - Versão 1.6.5 - Inicio dos testes de fluxo de segurança
- 13/06/2025 - Versão 1.6.6 - Testes de fluxo de segurança OK
- 14/06/2025 - Versão 1.6.7 - Implementados e testados os métodos GET de Segurança OK
- 02/07/2025 - Versão 1.6.8 - Implementados segurança por Role e por Permissão e email com logo.
- 16/02/2026 - Versão 1.6.9 - Implementado tempo e data de expiração do token.
- 21/02/2026 - Versão 1.7.0 - Correção do Erro de CORS.
- 21/02/2026 - Versão 1.7.1 - Adicionados dados para o frontend como nome do usuario e ROLE.
- 07/03/2026 - Versão 1.7.2 - Adicionado tratamento de erros e gerador de senhas para o primeiro acesso.
- 09/03/2026 - Versão 1.8.0 - Enviando o email de primeiro acesso, após a criação do usuário.
- 10/03/2026 - Versão 1.8.1 - Adicionado informações de Segurança e melhorando o CORS para chamadas com Authorization.
- 11/03/2026 - Versão 1.8.2 - Fluxo de Usuario 95% completo (front+backend).
- 12/03/2026 - Versão 1.8.3 - Logout funcionando e tratamento de erros de token expirado.
- 13/03/2026 - Versão 1.8.4 - Relatorio de usuários funcionando e gerando PDF
- 14/03/2026 - Versão 1.8.5 - Melhorias no layout do relatório, e Usuario 100% funcional.
- 16/03/2026 - Versão 1.8.6 - Melhorar primeiro acesso e Vulnerabilidades.
- 19/03/2026 - Versão 1.8.7 - Melhorias no primeiro acesso e tratamento de erros.
- 21/03/2026 - Versão 1.8.8 - Gerar PDF Unidade de Medida e Conversão.
- 22/03/2026 - Versão 1.8.9 - Melhorias no layout do PDF e correção de erros de conversão.
- 21/04/2026 - Versão 1.9.0 - Adicionado chamada de backend pata Itens.
- 22/04/2026 - Versão 1.9.1 - PDF de detalhe de Itens.
- 23/04/2026 - Versão 1.9.2 - Grafico de historico de Itens Gerado.
- 24/04/2026 - Versão 1.9.3 - Melhorias no layout do gráfico e correção de erros de Itens.
- 03/05/2026 - Versão 1.9.4 - Melhorias em ProdutoCompletoDTO.
- 03/05/2026 - Versão 1.9.5 - Adicionado Grafico de Valores dos Itens
- 21/05/2026 - Versão 1.9.6 - Adição de imagem ao produto.
- 28/05/2026 - Versão 1.9.7 - Testes Unitarios.
- 06/06/2026 - Versão 1.9.8 - Migração preparatória para PRODUÇÃO
- 07/06/2026 - Versão 1.9.9 - Novos testes e docker e documentação.
