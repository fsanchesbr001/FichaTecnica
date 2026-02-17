# Guia Prático: Configurando Expiração de JWT Token

## Exemplo 1: Configuração Rápida para Desenvolvimento

### Windows PowerShell
```powershell
# Abrir PowerShell como Administrador
# Definir a variável de ambiente para 30 minutos
[System.Environment]::SetEnvironmentVariable("JWT_EXPIRATION_MINUTES", "30", "Machine")

# Verificar se foi definida corretamente
Get-ChildItem env:JWT_EXPIRATION_MINUTES
```

### Linux/Mac (Bash)
```bash
# Adicionar ao ~/.bashrc ou ~/.zshrc
echo 'export JWT_EXPIRATION_MINUTES=30' >> ~/.bashrc
source ~/.bashrc

# Verificar
echo $JWT_EXPIRATION_MINUTES
```

## Exemplo 2: Testes Automatizados

### Teste 1: Token Válido
```bash
# 1. Login (obter token)
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"login":"seu_usuario","senha":"sua_senha"}'

# Resposta esperada:
# {
#   "jwt": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
# }

# 2. Usar o token imediatamente (deve funcionar)
curl -X GET http://localhost:8080/api/algumaRotaProtegida \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# Resposta esperada: 200 OK com dados
```

### Teste 2: Token Expirado
```bash
# 1. Configure JWT_EXPIRATION_MINUTES=1 para teste rápido
[System.Environment]::SetEnvironmentVariable("JWT_EXPIRATION_MINUTES", "1", "Machine")

# 2. Reinicie a aplicação

# 3. Faça login
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"login":"seu_usuario","senha":"sua_senha"}'

# 4. Aguarde 61 segundos

# 5. Tente usar o token
curl -X GET http://localhost:8080/api/algumaRotaProtegida \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# Resposta esperada: 401 Unauthorized
# {
#   "error": "Token expirado",
#   "message": "Seu token de autenticação expirou. Por favor, faça login novamente."
# }
```

## Exemplo 3: Diferentes Cenários

### Cenário 1: Application Web (Curto)
```bash
# Expiração: 15 minutos
[System.Environment]::SetEnvironmentVariable("JWT_EXPIRATION_MINUTES", "15", "Machine")
```

### Cenário 2: Mobile App (Médio)
```bash
# Expiração: 1 hora
[System.Environment]::SetEnvironmentVariable("JWT_EXPIRATION_MINUTES", "60", "Machine")
```

### Cenário 3: API Interna (Longo)
```bash
# Expiração: 8 horas
[System.Environment]::SetEnvironmentVariable("JWT_EXPIRATION_MINUTES", "480", "Machine")
```

## Exemplo 4: Implementação Client-Side (Frontend)

### JavaScript/TypeScript
```javascript
// Função para fazer login
async function login(username, password) {
  const response = await fetch('/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ login: username, senha: password })
  });
  
  if (response.ok) {
    const data = await response.json();
    // Armazenar token
    localStorage.setItem('jwtToken', data.jwt);
    // Armazenar tempo de expiração (opcional, para aviso ao usuário)
    const expirationTime = new Date().getTime() + (120 * 60 * 1000); // 120 min padrão
    localStorage.setItem('tokenExpiration', expirationTime);
    return true;
  }
  return false;
}

// Função para fazer requisição autenticada
async function fetchWithAuth(url, options = {}) {
  const token = localStorage.getItem('jwtToken');
  
  if (!token) {
    // Token não existe, redirecionar para login
    window.location.href = '/login';
    return;
  }
  
  const headers = {
    ...options.headers,
    'Authorization': `Bearer ${token}`
  };
  
  const response = await fetch(url, {
    ...options,
    headers
  });
  
  // Se retornar 401 (Unauthorized), o token expirou
  if (response.status === 401) {
    const errorData = await response.json();
    
    if (errorData.error === 'Token expirado') {
      // Limpar dados do usuário
      localStorage.removeItem('jwtToken');
      localStorage.removeItem('tokenExpiration');
      
      // Exibir aviso e redirecionar
      alert('Sua sessão expirou. Por favor, faça login novamente.');
      window.location.href = '/login';
      return;
    }
  }
  
  return response;
}

// Uso:
// await login('usuario', 'senha');
// const response = await fetchWithAuth('/api/items');
```

### React com Axios
```javascript
import axios from 'axios';

// Interceptor para adicionar token em todas as requisições
axios.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('jwtToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Interceptor para tratar tokens expirados
axios.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      const errorData = error.response.data;
      
      if (errorData.error === 'Token expirado') {
        // Limpar dados
        localStorage.removeItem('jwtToken');
        localStorage.removeItem('tokenExpiration');
        
        // Redirecionar para login
        window.location.href = '/login';
        alert('Sua sessão expirou. Por favor, faça login novamente.');
      }
    }
    
    return Promise.reject(error);
  }
);
```

## Exemplo 5: Monitoramento de Expiração no Backend

### Adicionar Logging (já implementado)
Os logs são registrados em:
- `WARN`: Token expirado
- `INFO`: Token válido
- `WARN`: Erro na verificação

Visualizar em: `target/logs/` ou console da aplicação

### Criar Métrica (Opcional)
```java
// Adicionar ao TokenService
@Value("${api.security.token.expiration-minutes:120}")
private long expirationMinutes;

public long getTokenExpirationMinutes() {
    return expirationMinutes;
}
```

## Exemplo 6: Verificar Configuração Atual

### Via Properties
```java
// Injetar em um Controller para debug
@GetMapping("/token-config")
public ResponseEntity<Map<String, Object>> getTokenConfig() {
    Map<String, Object> config = new HashMap<>();
    config.put("expirationMinutes", expirationMinutes);
    config.put("expirationHours", expirationMinutes / 60.0);
    return ResponseEntity.ok(config);
}
```

### Via Terminal
```bash
# Windows - Verificar variável de ambiente
echo %JWT_EXPIRATION_MINUTES%

# Linux/Mac
echo $JWT_EXPIRATION_MINUTES
```

## Troubleshooting

### Problema: Variável de ambiente não está sendo lida

**Solução 1**: Reiniciar a IDE/Aplicação após definir a variável
```bash
# Certifique-se de que a aplicação foi reiniciada
# A variável de ambiente é lida ao iniciar
```

**Solução 2**: Usar arquivo `application-development.properties`
```properties
# Em src/main/resources/application-development.properties
api.security.token.expiration-minutes=30
```

### Problema: Token continua expirando rápido

**Verificar**:
1. Qual é o valor de `JWT_EXPIRATION_MINUTES`
2. Logs da aplicação mostram qual valor está sendo usado
3. Confirmar que está em MINUTOS, não segundos

### Problema: Token não expira

**Verificar**:
1. Se variável não está definida, usa padrão de 120 minutos
2. Se quiser reduzir, defina explicitamente `JWT_EXPIRATION_MINUTES`
3. Logs devem mostrar: `Token válido e não expirado`

## Resumo Rápido

| Ação | Comando |
|------|---------|
| **Windows**: Definir 60 min | `[System.Environment]::SetEnvironmentVariable("JWT_EXPIRATION_MINUTES", "60", "Machine")` |
| **Linux**: Definir 60 min | `export JWT_EXPIRATION_MINUTES=60` |
| **Testar Token Válido** | `curl -H "Authorization: Bearer TOKEN" http://localhost:8080/api/...` |
| **Testar Token Expirado** | Aguarde mais que o tempo configurado e repita |
| **Verificar Config** | Logs ou GET `/token-config` |

---

**Nota**: Sempre reinicie a aplicação após alterar a variável de ambiente!

