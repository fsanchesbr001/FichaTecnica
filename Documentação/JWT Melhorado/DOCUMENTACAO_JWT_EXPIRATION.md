# Documentação: Expiração de JWT Token com Variável de Sistema

## Resumo das Mudanças

Foi implementado um sistema completo de expiração configurável para JWT Tokens, permitindo que o tempo máximo de validade seja definido via variável de sistema do SO. Após a expiração, o token é automaticamente invalidado e o usuário é forçado a fazer um novo login.

## Arquivos Modificados

### 1. `application.properties`
Adicionada nova propriedade de configuração:
```properties
api.security.token.expiration-minutes=${JWT_EXPIRATION_MINUTES:120}
```

- **JWT_EXPIRATION_MINUTES**: Variável de sistema que define o tempo de expiração em minutos
- **Padrão**: 120 minutos (2 horas) se a variável não for definida

### 2. `TokenService.java`
Implementadas as seguintes mudanças:

#### a) Injeção de Propriedade
```java
@Value("${api.security.token.expiration-minutes:120}")
private long expirationMinutes;
```

#### b) Método `dataExpiracao()` Atualizado
Agora utiliza a variável configurável em vez de um valor fixo:
```java
private Instant dataExpiracao() {
    return LocalDateTime.now().plusMinutes(expirationMinutes)
        .toInstant(ZoneOffset.of("-03:00"));
}
```

#### c) Novo Método: `validarTokenExpirado()`
Valida se o token ainda é válido e não expirou:
```java
public boolean validarTokenExpirado(String tokenJWT) {
    try {
        logger.info("Validando expiração do token");
        var algoritimo = Algorithm.HMAC256(secret);
        JWT.require(algoritimo)
                .withIssuer("API Ficha Tecnica")
                .build()
                .verify(tokenJWT);
        logger.info("Token válido e não expirado");
        return true;
    } catch (TokenExpiredException exception) {
        logger.warn("Token expirado: {}", exception.getMessage());
        return false;
    } catch (JWTVerificationException exception) {
        logger.warn("Erro ao verificar token: {}", exception.getMessage());
        return false;
    }
}
```

#### d) Import Adicionado
```java
import com.auth0.jwt.exceptions.TokenExpiredException;
```

### 3. `SecurityFilter.java`
Melhorado o filtro de segurança para validar tokens expirados:

#### a) Novos Imports
```java
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
```

#### b) Validação de Expiração
Adicionada verificação de expiração do token antes de processar a requisição:
```java
// Validar se o token está expirado
if(!tokenService.validarTokenExpirado(tokenJWT)){
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json");
    Map<String, String> errorMap = new HashMap<>();
    errorMap.put("error", "Token expirado");
    errorMap.put("message", "Seu token de autenticação expirou. Por favor, faça login novamente.");
    response.getWriter().write(new ObjectMapper().writeValueAsString(errorMap));
    return;
}
```

#### c) Tratamento de Erros Melhorado
Adicionado try-catch para capturar erros na validação do token:
```java
try {
    var subject = tokenService.getSubject(tokenJWT);
    var role = tokenService.getRole(tokenJWT);
    // ... resto do código
} catch (Exception e) {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json");
    Map<String, String> errorMap = new HashMap<>();
    errorMap.put("error", "Token inválido");
    errorMap.put("message", "Seu token de autenticação é inválido. Por favor, faça login novamente.");
    response.getWriter().write(new ObjectMapper().writeValueAsString(errorMap));
    return;
}
```

## Como Usar

### 1. Tempo de Expiração Padrão
Se nenhuma variável de sistema for definida, o sistema usará o padrão de **120 minutos** (2 horas).

### 2. Definir Variável de Sistema (Windows)
Para definir a expiração via variável de ambiente Windows:

```powershell
# PowerShell (como Administrador)
[System.Environment]::SetEnvironmentVariable("JWT_EXPIRATION_MINUTES", "60", "Machine")
```

Ou através das variáveis de ambiente do Windows:
1. Pressione `Win + X` e selecione "System (Configurações)"
2. Clique em "Advanced system settings"
3. Clique em "Environment Variables"
4. Clique em "New" e adicione:
   - **Variable name**: `JWT_EXPIRATION_MINUTES`
   - **Variable value**: `60` (ou o valor desejado em minutos)

### 3. Definir Variável de Sistema (Linux/Mac)
```bash
export JWT_EXPIRATION_MINUTES=60
```

Ou adicionar ao arquivo `.bashrc` ou `.zshrc`:
```bash
export JWT_EXPIRATION_MINUTES=60
```

### 4. Definir via Docker (se aplicável)
```dockerfile
ENV JWT_EXPIRATION_MINUTES=60
```

### 5. Definir via Docker Compose
```yaml
environment:
  - JWT_EXPIRATION_MINUTES=60
```

## Comportamento do Sistema

### Quando um token expira:
1. O cliente faz uma requisição com um token expirado
2. O `SecurityFilter` intercepta a requisição
3. O método `validarTokenExpirado()` valida o token
4. Se expirado, retorna **HTTP 401 (Unauthorized)** com mensagem:
   ```json
   {
     "error": "Token expirado",
     "message": "Seu token de autenticação expirou. Por favor, faça login novamente."
   }
   ```
5. O cliente deve redirecionar o usuário para a tela de login

### Quando um token é inválido:
Retorna **HTTP 401 (Unauthorized)** com mensagem:
```json
{
  "error": "Token inválido",
  "message": "Seu token de autenticação é inválido. Por favor, faça login novamente."
}
```

## Exemplos de Tempo de Expiração Recomendados

| Tipo de Aplicação | Tempo Recomendado |
|-------------------|------------------|
| Web Application   | 30 - 60 minutos   |
| Mobile App        | 60 - 120 minutos  |
| API Confidencial  | 15 - 30 minutos   |
| API Pública       | 240 - 480 minutos |

## Logs

O sistema registra todas as operações de validação de token:
- `Token válido e não expirado` - Token passou na validação
- `Token expirado: ...` - Token expirou
- `Erro ao verificar token: ...` - Erro na validação do token

## Testing

Para testar a expiração em desenvolvimento:
1. Defina `JWT_EXPIRATION_MINUTES=1` para um token com 1 minuto de validade
2. Faça login e receba o token
3. Aguarde mais de 1 minuto
4. Faça uma requisição com o token expirado
5. Deverá receber erro 401 com mensagem de token expirado

## Segurança

- ✅ Tokens são validados em cada requisição
- ✅ Expiração é verificada antes de extrair dados do token
- ✅ Erros são tratados adequadamente
- ✅ Respostas de erro não expõem detalhes internos da aplicação
- ✅ Tokens expirados forçam novo login

## Suporte a Refresh Token (Opcional)

Para implementar refresh tokens no futuro:
1. Adicionar campo de refresh token na classe `Usuario`
2. Criar novo endpoint `/auth/refresh` que aceita o refresh token
3. Validar o refresh token (com tempo maior de expiração)
4. Gerar novo JWT token se refresh token for válido

## Conclusão

O sistema agora possui controle total sobre a expiração de tokens JWT, permitindo:
- ✅ Configuração via variável de sistema
- ✅ Validação automática em cada requisição
- ✅ Mensagens claras de erro
- ✅ Forçar novo login após expiração
- ✅ Melhor segurança e controle de acesso

