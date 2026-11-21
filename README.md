# Projeto Ficha Técnica
## Descrição
Este projeto visa a criação de uma ficha técnica de um produto, onde é possível adicionar, editar e excluir produtos, além de visualizar a lista de produtos cadastrados.
## Tecnologias
- Java 17
- Spring Boot 3.4.1

## Dependências
- Spring Data JPA

## E-mail: Transactional Outbox e API Zoho

O cadastro grava `usuarios`, `seguranca` e `email_outbox` na mesma transacao
MySQL. Recuperacao de senha e primeiro acesso tambem enfileiram o e-mail na
transacao dos dados de seguranca. Uma falha ao gravar/renderizar a mensagem
desfaz os dados; o envio remoto nunca acontece na requisicao HTTP. Os endpoints
mantem HTTP 200 por compatibilidade: significa **dados gravados e e-mail
enfileirado**, nao entregue. Chamadores novos de `EnviarEmailPort` precisam de
uma transacao ativa; a outbox exige `MANDATORY`.

A migracao Flyway `V003__Criar-email-outbox.sql` cria a tabela no deploy.
O worker consulta a cada 5 segundos, processa ate 20 mensagens por rodada e
faz o envio fora da transacao de banco. Claims usam `FOR UPDATE SKIP LOCKED`,
lease de 2 minutos e token de ownership. Se o processo cair, outro worker
recupera a mensagem apos o lease. Falhas geram retries persistidos com backoff
de 30 segundos, dobrando ate 1 hora; nao existe limite de tentativas enquanto
a mensagem estiver valida. Nao aumente os timeouts do transporte acima do lease.

Emails de credenciais expiram conforme a senha/token salvo em `seguranca`;
mensagens expiradas nao sao enviadas. Novas credenciais cancelam mensagens
anteriores ainda pendentes do mesmo destinatario/template. Um envio ja em
andamento nao pode ser desfeito. Estados: `PENDING`, `PROCESSING`, `SENT`,
`EXPIRED` e `CANCELLED`. `SENT` indica aceite pelo provedor, nao recebimento
na caixa de entrada. A garantia e **pelo menos uma vez**: uma queda entre o
aceite da Zoho e a confirmacao no banco pode duplicar o e-mail. A API utilizada
nao oferece uma chave de idempotencia para eliminar essa janela.

Um rollback para uma imagem anterior a esta implementacao interrompe o worker,
mas nao remove eventos da tabela. Retome uma versao com outbox para drenar a
fila; nao remova a tabela nem sua chave durante um rollback.

O payload, incluindo senha temporaria/token, fica protegido com AES-256-GCM e
IV aleatorio. Ele e apagado ao enviar, expirar ou cancelar. A chave
`OUTBOX_ENCRYPTION_KEY` e obrigatoria, independente do JWT e deve ser mantida
entre deploys e junto aos backups. Nao a regenere enquanto houver mensagens
pendentes/em processamento. Logs e `last_error` registram apenas ID, tentativa
e codigo de erro, sem credenciais, destinatarios ou conteudo.

### Preparar a Zoho antes do deploy

1. Configure um cliente OAuth da Zoho e autorize a conta remetente com
   `ZohoMail.messages.CREATE`. Para consultar o account ID pela API, autorize
   tambem `ZohoMail.accounts.READ`. Obtenha client ID, client secret, refresh
   token e account ID. Pode usar o mesmo cliente/conta do Contatos, se os
   escopos forem adequados; nao copie valores para arquivos versionados.
2. Cadastre os GitHub Actions Secrets `ZOHO_CLIENT_ID`, `ZOHO_CLIENT_SECRET`,
   `ZOHO_REFRESH_TOKEN`, `ZOHO_ACCOUNT_ID` e `OUTBOX_ENCRYPTION_KEY`.
   A chave deve conter exatamente 32 bytes aleatorios codificados em Base64.
   Para um deploy manual Linux, gere uma unica vez em um diretorio privado:
   `umask 077; mkdir -p docker/secrets; chmod 700 docker/secrets; openssl rand -base64 32 > docker/secrets/outbox_encryption_key.txt`.
3. Defina `MAIL_FROM` no `APP_ENV_FILE` com a conta/alias autorizado pelo
   account ID (padrao: `contato@fabriciosanches.com`). O workflow seleciona
   `MAIL_PROVIDER=zoho`, valida os secrets e cria arquivos protegidos para
   OAuth/chave da outbox. O Compose e o entrypoint ja os carregam via `_FILE`.
   `MAIL_PWD` nao e exigido pelo deploy Zoho; seu arquivo permanece montado
   apenas para compatibilidade SMTP.
4. Faca backup consistente do MySQL e da chave antes de aplicar a migracao.
   Apos o deploy, cadastre um usuario de teste autorizado, verifique a outbox
   e confirme o recebimento do e-mail. Nao e necessario liberar portas SMTP:
   OAuth usa `https://accounts.zoho.com/oauth/v2/token`, e o envio usa
   `https://mail.zoho.com/api/accounts/{accountId}/messages`, ambos na porta 443.

Em deploy manual, preencha `.env.prod` a partir de `.env.prod.example` e crie
os arquivos reais indicados por `ZOHO_CLIENT_ID_FILE`, `ZOHO_CLIENT_SECRET_FILE`,
`ZOHO_REFRESH_TOKEN_FILE` e `OUTBOX_ENCRYPTION_KEY_FILE`. Preserve os demais
secrets existentes. No Compose standalone, os secrets de arquivo sao bind
mounts: o backend roda como `appuser`, portanto arquivos root com modo `0600`
nao sao legiveis. Use diretorio `docker/secrets` com modo `0700` e arquivos
montados com modo `0644`, como faz o workflow. O diretorio privado impede
acesso de outros usuarios no host; o bind mount permite leitura no container.
Em desenvolvimento, defina `OUTBOX_ENCRYPTION_KEY` no
ambiente da IDE ou no `.env` do Compose; `MAIL_PROVIDER=smtp` continua sendo
o padrao. Para testar Zoho localmente, selecione `MAIL_PROVIDER=zoho` e preencha
as variaveis OAuth. Contas de outro datacenter exigem ajustar as propriedades
`ficha-tecnica.mail.zoho.accounts-url` e `ficha-tecnica.mail.zoho.mail-url`.

Os timeouts Zoho sao 5 segundos para conexao e 20 segundos por requisicao.
O worker pode ser suspenso com `MAIL_OUTBOX_WORKER_ENABLED=false` sem perder
eventos; reative-o para retomar o envio. Monitore falhas e idade da fila:

```sql
SELECT status, COUNT(*) AS quantidade, MIN(created_at) AS mais_antigo
FROM email_outbox GROUP BY status;
SELECT id, status, attempts, next_attempt_at, last_error
FROM email_outbox WHERE status IN ('PENDING', 'PROCESSING', 'EXPIRED');
```

Corrigir credenciais/conectividade permite o retry automatico. Para eventos
`EXPIRED`, gere novas credenciais pelo fluxo de primeiro acesso/recuperacao;
nao reenfileire a senha/token vencido. O usuario criado no incidente anterior
nao tem evento de outbox retroativo: use primeiro acesso depois de configurar
a Zoho. Nao recrie o mesmo login nem tente recuperar a senha do hash.

## Docker (Backend + MySQL + Observabilidade)

Arquivos adicionados na raiz do projeto:
- `Dockerfile`
- `docker-compose.yml`
- `docker-compose.prod.yml`
- `.env.example`
- `.env.prod.example`
- `.dockerignore`
- `docker/prometheus/prometheus.yml`

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
docker compose --env-file .env.prod -f docker-compose.prod.yml up --build -d
```

O Compose de producao e independente; nao o sobreponha ao Compose de
desenvolvimento, que publica outras portas e usa outro esquema de senhas.

### Producao em Droplets separados

O backend Ollivander permanece em `127.0.0.1:8080:8080`; nao precisa mudar
para 8081 nem expor a porta 8080 publicamente. No Droplet `159.203.68.36`,
um Nginx no host deve encaminhar
`https://ollivander.backend.fabriciosanches.com` para
`http://127.0.0.1:8080`, preservando o caminho `/auth/login`.
A configuracao compartilhada com `backend.fabriciosanches.com`, o DNS,
os certificados e o rollback estao em
`deploy/README.shared-proxy.md` do repositorio `ContatosSite`
([PR #31](https://github.com/fsanchesbr001/ContatosSite/pull/31)).
Essa migracao e um pre-requisito para a entrada HTTPS publica; este
repositorio nao instala nem ativa o Nginx do host.

O fluxo e: navegador -> `https://ollivander.fabriciosanches.com`
(frontend/Nginx no outro Droplet) -> `https://ollivander.backend.fabriciosanches.com`
(Nginx compartilhado em `159.203.68.36`) -> `http://127.0.0.1:8080`
(backend). As portas publicas 80/443 do Droplet do backend pertencem ao
Nginx/ACME; nao abra 8080 no firewall.

Defina `SITE_ORIGIN_ALLOWED=https://ollivander.fabriciosanches.com` no
ambiente real de producao (`APP_ENV_FILE` no deploy, se utilizado). O valor
padrao nao substitui um valor antigo explicitamente definido no `.env`.
Variaveis exportadas no shell prevalecem sobre o `.env`; `--env-file`
seleciona outro arquivo em lugar do `.env` carregado automaticamente.
`APP_ENV_FILE` e o conteudo completo do arquivo, nao seu caminho: quando
preenchido no workflow, substitui o `.env` do servidor antes do deploy.
Essa origem e a do frontend, nao a da API: o Spring valida o header `Origin`
mesmo quando a requisicao passa pelos proxies.

O servico `frontend` agora e opcional, pelo profile `frontend`, para que
o deploy do backend nao crie um frontend extra na porta publica 80 e
dispute a entrada HTTP/ACME do Nginx compartilhado. O pipeline existente
continua iniciando somente os servicos sem profile (MySQL e backend).
Um frontend antigo ja em execucao nao e parado por essa mudanca. Antes de
transferir as portas publicas para o Nginx, pare somente essa UI legada no
diretorio de producao:

```bash
docker compose -f docker-compose.prod.yml --profile frontend stop frontend
```

Se houver necessidade de uma UI local explicita:

```bash
docker compose --env-file .env.prod -f docker-compose.prod.yml --profile frontend up -d
```

Nesse caso, a UI publica `127.0.0.1:8081:80`; isso nao substitui o frontend
no outro Droplet nem configura automaticamente um proxy de entrada.
`FRONTEND_EXTERNAL_PORT` nao altera essa porta fixa. O upstream padrao e
`https://ollivander.backend.fabriciosanches.com`; remova ou corrija um
`BACKEND_API_URL` antigo se habilitar essa UI.

#### Validacao antes e depois do deploy

O merge em `master` inicia o deploy existente, que transfere o Compose
aprovado para o diretorio configurado em `DO_PROJECT_DIR` antes de atualizar
a imagem. O fallback de `DO_PROJECT_DIR` e `/opt/fichatecnica`. Confira esse
caminho e o `APP_ENV_FILE` antes do merge. O deploy executa `up -d --no-build
backend`, incluindo sua dependencia MySQL, sem iniciar a UI nem remover volumes.

Na raiz do repositorio, estes comandos validam a configuracao sem iniciar
containers (na ausencia de ambiente real, usam os defaults e segredos de exemplo):

```bash
docker compose -f docker-compose.prod.yml config --format json
docker compose -f docker-compose.prod.yml --profile frontend config --format json
git diff --check
git status --short --branch
```

Sem profile, espere apenas `mysql` e `backend`; com `frontend`, espere tambem
a UI com `profiles: ["frontend"]`. Confirme `host_ip: 127.0.0.1`, porta publicada
8080/target 8080 para o backend e publicada 8081/target 80 para a UI.
Confirme tambem a origem permitida, o upstream HTTPS e os volumes persistentes
`mysql-data` e `app-images-data`. No servidor, repita a validacao com o `.env`
efetivo; se operar manualmente com `.env.prod`, acrescente `--env-file .env.prod`.
Nao publique o JSON do ambiente real, que pode conter valores sensiveis.

Depois do deploy e da migracao do Nginx, execute no Droplet do backend:

```bash
docker compose -f docker-compose.prod.yml ps
curl -fsS http://127.0.0.1:8080/actuator/health
sudo nginx -t
sudo ss -ltnp '( sport = :80 or sport = :443 or sport = :8080 or sport = :8081 )'
curl -fsS https://ollivander.backend.fabriciosanches.com/actuator/health
curl -fi -X OPTIONS https://ollivander.backend.fabriciosanches.com/auth/login \
  -H 'Origin: https://ollivander.fabriciosanches.com' \
  -H 'Access-Control-Request-Method: POST' \
  -H 'Access-Control-Request-Headers: content-type'
```

Espere health `UP`, 8080 somente em loopback e o header
`Access-Control-Allow-Origin: https://ollivander.fabriciosanches.com` no preflight.
Valide tambem o frontend e seu login pelo navegador no outro Droplet.
Nao envie credenciais reais para diagnosticar conectividade.

#### Rollback seguro

Antes do merge (o SCP do workflow sobrescreve o Compose), no servidor,
salve a configuracao e a imagem atual em uma sessao administrativa:

```bash
cd "${DO_PROJECT_DIR:-/opt/fichatecnica}" || exit 1
umask 077
BACKUP_DIR="$(mktemp -d "$PWD/rollback.XXXXXXXX")"
cp -p docker-compose.prod.yml .env "$BACKUP_DIR/"
cp -a docker/secrets "$BACKUP_DIR/secrets"
docker inspect -f '{{.Config.Image}}' ficha-backend > "$BACKUP_DIR/backend-image.txt"
printf 'Backup para rollback: %s\n' "$BACKUP_DIR"
```

Interrompa o procedimento se algum comando falhar. Guarde o caminho exibido;
o backup contem segredos e nao deve ser versionado. Preserve tambem backups
consistentes do MySQL e das imagens da aplicacao antes de qualquer migracao
de dados; restaurar configuracao nao reverte schema nem dados.

Em caso de falha, suspenda novos deploys e restaure no mesmo diretorio
(defina `BACKUP_DIR` com o caminho salvo se abriu outra sessao):

```bash
cp -p "$BACKUP_DIR/docker-compose.prod.yml" "$BACKUP_DIR/.env" .
cp -a "$BACKUP_DIR/secrets/." docker/secrets/
docker compose -f docker-compose.prod.yml config --quiet
BACKEND_IMAGE="$(cat "$BACKUP_DIR/backend-image.txt")" \
  docker compose -f docker-compose.prod.yml up -d --no-build backend
curl -fsS http://127.0.0.1:8080/actuator/health
```

Repita as validacoes de health/HTTPS acima apos a estabilizacao. O workflow
reverte automaticamente apenas a imagem se o healthcheck falhar; ele nao
restaura Compose, `.env`, segredos, Nginx ou dados. As imagens anteriores nao
sao mais apagadas automaticamente; planeje sua retencao monitorando o disco.
Antes de reativar a pipeline, corrija tambem `APP_ENV_FILE` se ele for a causa.
Nao execute `down -v`, `docker volume prune` nem remova `mysql-data` ou
`app-images-data`. Nao execute um `up` geral do Compose antigo: ele pode iniciar
uma UI na porta publica 80. Para reverter a entrada HTTPS compartilhada,
siga separadamente o runbook do `ContatosSite#31`.

### Ver logs do backend

```bash
docker compose --env-file .env logs -f backend
```

### Ver logs da observabilidade

```bash
docker compose --env-file .env logs -f prometheus grafana
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

## Observabilidade (Actuator + Prometheus + Grafana)

Com a aplicação em execução:

- Health geral: `http://localhost:8080/actuator/health`
- Liveness: `http://localhost:8080/actuator/health/liveness`
- Readiness: `http://localhost:8080/actuator/health/readiness`
- Métricas Prometheus: `http://localhost:8080/actuator/prometheus`
- Prometheus UI: `http://localhost:9090`
- Grafana UI: `http://localhost:3000`

Credenciais padrão do Grafana:

- Usuário: `admin`
- Senha: valor de `GRAFANA_ADMIN_PASSWORD` (default `admin`)

### Configurar datasource no Grafana

1. Acesse `Connections` → `Data Sources` → `Add data source`.
2. Selecione `Prometheus`.
3. URL: `http://prometheus:9090`.
4. Clique em `Save & test`.

### Dashboards sugeridos

- Importar dashboard JVM Micrometer: ID `4701`.
- Importar dashboard Spring Boot: ID `11378`.

Query para taxa de requisições por endpoint de Unidade de Medida:

```promql
sum(rate(http_server_requests_seconds_count{uri=~"/ficha-tecnica/unidades-medida.*"}[1m])) by (uri, method, status)
```

Query para latência média das requisições de Unidade de Medida:

```promql
sum(rate(http_server_requests_seconds_sum{uri=~"/ficha-tecnica/unidades-medida.*"}[1m]))
/
sum(rate(http_server_requests_seconds_count{uri=~"/ficha-tecnica/unidades-medida.*"}[1m]))
```

## Documentação
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Java 17](https://docs.oracle.com/en/java/javase/17/)
- [Maven](https://maven.apache.org/)

## Próximos Passos

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
- 01/07/2026 - Versão 2.0.0 - Migração para Clean Architecture (Arquitetura Hexagonal) - Inicio Unidade Medida.
- 02/07/2026 - Versão 2.0.1 - Migração para Clean Architecture (Arquitetura Hexagonal) - CRUD Unidade de Medida Completo.
- 05/07/2026 - Versão 2.0.2 - Migração para Clean Architecture (Arquitetura Hexagonal) - Unidade de Medida completa com Grafana e docker OK
- 27/08/2026 - Versão 2.0.5 - Migração para Clean Architecture (Arquitetura Hexagonal) - CRUD Conversão Completo.
- 06/09/2026 - Versão 2.0.6 - Migração para Clean Architecture (Arquitetura Hexagonal) - CRUD Item e Historico Item Completo.
- 08/09/2026 - Versão 2.0.7 - Migração para Clean Architecture (Arquitetura Hexagonal) - CRUD Produto e ItemProduto Completo.
- 09/09/2026 - Versão 2.0.8 - Migração para Clean Architecture (Arquitetura Hexagonal) - CRUD Usuario e Segurança Completo.
- 10/09/2026 - Versão 2.0.9 - Migração para Clean Architecture (Arquitetura Hexagonal) - Organizando Estrutura.
- 11/09/2026 - Versão 2.1.0 - Migração para Clean Architecture (Arquitetura Hexagonal) - Finalizando a migração hexagonal.
- 18/09/2026 - Versão 2.1.1 - Migração para Clean Architecture (Arquitetura Hexagonal) - Ajustes finais e melhorias de layout.
- 19/09/2026 - Versão 2.1.2 - Migração para Clean Architecture (Arquitetura Hexagonal) - Testes de Usuario e correção de rotas.
- 23/09/2026 - Versão 2.1.3 - Tratando Usuario System.
- 26/09/2026 - Versão 2.1.4 - Correções da carga e exibição de imagens.
- 11/10/2026 - Versão 2.1.5 - Preparando CI/CD para deploy em produção.
- 24/10/2026 - Versão 2.1.6 - Mapeando porta do banco para exposição no docker.
- 21/11/2026 - Versão 2.2.0 - Atualização de dependências e melhorias gerais.
