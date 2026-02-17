# Saída de Expiração do Token no Login

## Alterações Realizadas

### 1. `DadosTokenJWT.java` (Atualizado)
Agora inclui dois campos adicionais:
- `expirationMinutes`: Tempo de expiração em minutos
- `expiresAt`: Data e hora em que o token expira (formato ISO 8601)

```java
public record DadosTokenJWT(String jwt, Long expirationMinutes, Instant expiresAt) {
    
    // Construtor compatível com código antigo que passa apenas jwt
    public DadosTokenJWT(String jwt) {
        this(jwt, null, null);
    }
}
```

### 2. `TokenService.java` (Novo Método)
Adicionados dois novos métodos públicos:

```java
public long getExpirationMinutes() {
    return expirationMinutes;
}

public Instant getTokenExpiresAt() {
    return dataExpiracao();
}
```

### 3. `AutenticacaoController.java` (Atualizado)
Método `efetuarLogin` agora retorna expiração:

```java
@PostMapping("/login")
public ResponseEntity<DadosTokenJWT> efetuarLogin(@RequestBody @Valid AutenticacaoDTO dados){
    // ... validações ...
    var token = tokenService.gerarToken((Usuario) authentication.getPrincipal());
    var expirationMinutes = tokenService.getExpirationMinutes();
    var expiresAt = tokenService.getTokenExpiresAt();
    return ResponseEntity.ok(new DadosTokenJWT(token, expirationMinutes, expiresAt));
}
```

## Exemplo de Resposta do Login

### Request
```bash
POST /auth/login
Content-Type: application/json

{
  "login": "usuario@exemplo.com",
  "senha": "senha123"
}
```

### Response (200 OK)
```json
{
  "jwt": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJBUEkgRmljaGEgVGVjbmljYSIsInN1YiI6InVzdWFyaW9AZXhlbXBsby5jb20iLCJyb2xlIjoiUk9MRV9VU0VSIiwibm9tZSI6Ikpvw6NvIiwiZXhwIjoxNzM5NzI1NjAwfQ.abc123...",
  "expirationMinutes": 120,
  "expiresAt": "2026-02-16T19:30:00Z"
}
```

### Explicação dos Campos

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `jwt` | String | Token JWT para usar nas requisições autenticadas |
| `expirationMinutes` | Long | Tempo de validade do token em minutos (configurável via `JWT_EXPIRATION_MINUTES`) |
| `expiresAt` | Instant | Data e hora em que o token expira (ISO 8601) |

## Exemplos com Diferentes Configurações

### Exemplo 1: Token com 30 minutos de expiração
```
JWT_EXPIRATION_MINUTES=30
```
Response:
```json
{
  "jwt": "eyJhbGciOiJIUzI1NiI...",
  "expirationMinutes": 30,
  "expiresAt": "2026-02-16T17:30:00Z"
}
```

### Exemplo 2: Token com 1 hora de expiração
```
JWT_EXPIRATION_MINUTES=60
```
Response:
```json
{
  "jwt": "eyJhbGciOiJIUzI1NiI...",
  "expirationMinutes": 60,
  "expiresAt": "2026-02-16T18:00:00Z"
}
```

### Exemplo 3: Token com 8 horas de expiração
```
JWT_EXPIRATION_MINUTES=480
```
Response:
```json
{
  "jwt": "eyJhbGciOiJIUzI1NiI...",
  "expirationMinutes": 480,
  "expiresAt": "2026-02-17T00:00:00Z"
}
```

## Uso no Frontend

### JavaScript/TypeScript
```javascript
// Login e armazenar informações de expiração
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
    
    // Armazenar informações de expiração
    localStorage.setItem('expirationMinutes', data.expirationMinutes);
    localStorage.setItem('expiresAt', data.expiresAt);
    
    // Exibir informação ao usuário
    console.log(`Token expira em ${data.expirationMinutes} minutos`);
    console.log(`Às: ${new Date(data.expiresAt).toLocaleString()}`);
    
    return true;
  }
  return false;
}

// Função para verificar se token está próximo de expirar
function isTokenExpiringSoon(minutesThreshold = 5) {
  const expiresAt = new Date(localStorage.getItem('expiresAt'));
  const now = new Date();
  const minutesUntilExpiration = (expiresAt - now) / (1000 * 60);
  
  return minutesUntilExpiration <= minutesThreshold;
}

// Usar em um componente React
function useTokenExpiration() {
  const [expiresIn, setExpiresIn] = React.useState(null);
  
  React.useEffect(() => {
    const expiresAt = new Date(localStorage.getItem('expiresAt'));
    
    const interval = setInterval(() => {
      const now = new Date();
      const minutesRemaining = (expiresAt - now) / (1000 * 60);
      setExpiresIn(Math.round(minutesRemaining));
      
      if (minutesRemaining <= 0) {
        // Token expirou
        localStorage.clear();
        window.location.href = '/login';
      }
    }, 1000);
    
    return () => clearInterval(interval);
  }, []);
  
  return expiresIn;
}

// Usar em JSX
function Dashboard() {
  const minutesLeft = useTokenExpiration();
  
  return (
    <div>
      <p>Seu token expira em: {minutesLeft} minutos</p>
    </div>
  );
}
```

### React com Axios
```javascript
import axios from 'axios';
import React from 'react';

// Após login bem-sucedido
async function handleLogin(username, password) {
  try {
    const response = await axios.post('/auth/login', {
      login: username,
      senha: password
    });
    
    const { jwt, expirationMinutes, expiresAt } = response.data;
    
    // Armazenar token e expiração
    localStorage.setItem('jwtToken', jwt);
    localStorage.setItem('expirationMinutes', expirationMinutes);
    localStorage.setItem('expiresAt', expiresAt);
    
    // Configurar header padrão do axios
    axios.defaults.headers.common['Authorization'] = `Bearer ${jwt}`;
    
    // Mostrar ao usuário
    alert(`Login realizado! Token expira em ${expirationMinutes} minutos`);
    
  } catch (error) {
    console.error('Erro ao fazer login:', error);
  }
}

// Componente de contador de expiração
function TokenExpirationTimer() {
  const [remaining, setRemaining] = React.useState(null);
  
  React.useEffect(() => {
    const expiresAt = new Date(localStorage.getItem('expiresAt'));
    
    const timer = setInterval(() => {
      const now = new Date();
      const diff = expiresAt - now;
      const minutes = Math.floor(diff / (1000 * 60));
      const seconds = Math.floor((diff % (1000 * 60)) / 1000);
      
      if (minutes <= 0 && seconds <= 0) {
        localStorage.clear();
        window.location.href = '/login';
      } else {
        setRemaining(`${minutes}:${seconds.toString().padStart(2, '0')}`);
      }
    }, 1000);
    
    return () => clearInterval(timer);
  }, []);
  
  return (
    <div style={{
      padding: '10px',
      backgroundColor: remaining && remaining.startsWith('0:') ? '#ff6b6b' : '#51cf66',
      color: 'white',
      borderRadius: '4px',
      textAlign: 'center'
    }}>
      Sessão expira em: {remaining}
    </div>
  );
}
```

### HTML/JavaScript Puro
```html
<!DOCTYPE html>
<html>
<head>
  <style>
    .token-timer {
      padding: 10px;
      text-align: center;
      border-radius: 4px;
      font-weight: bold;
      margin: 10px 0;
    }
    
    .token-timer.warning {
      background-color: #ffc107;
      color: black;
    }
    
    .token-timer.danger {
      background-color: #dc3545;
      color: white;
    }
    
    .token-timer.safe {
      background-color: #28a745;
      color: white;
    }
  </style>
</head>
<body>
  <div id="tokenTimer" class="token-timer safe">
    Sessão expira em: -- minutos
  </div>

  <script>
    function displayTokenExpiration() {
      const expiresAt = localStorage.getItem('expiresAt');
      const expirationMinutes = localStorage.getItem('expirationMinutes');
      
      if (!expiresAt) {
        document.getElementById('tokenTimer').innerHTML = 'Não autenticado';
        return;
      }
      
      const timer = setInterval(() => {
        const now = new Date();
        const expires = new Date(expiresAt);
        const diff = expires - now;
        const minutes = Math.floor(diff / (1000 * 60));
        const seconds = Math.floor((diff % (1000 * 60)) / 1000);
        
        const timerElement = document.getElementById('tokenTimer');
        
        if (minutes <= 0 && seconds <= 0) {
          clearInterval(timer);
          localStorage.clear();
          window.location.href = '/login';
        } else {
          timerElement.innerHTML = `Sessão expira em: ${minutes}:${seconds.toString().padStart(2, '0')}`;
          
          // Mudar cor de acordo com tempo restante
          if (minutes <= 5) {
            timerElement.className = 'token-timer danger';
          } else if (minutes <= 15) {
            timerElement.className = 'token-timer warning';
          } else {
            timerElement.className = 'token-timer safe';
          }
        }
      }, 1000);
    }
    
    // Chamar quando a página carregar
    window.addEventListener('load', displayTokenExpiration);
  </script>
</body>
</html>
```

## Testes com cURL

### Teste 1: Login com Token
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"login":"seu_usuario","senha":"sua_senha"}' | jq .
```

Resposta:
```json
{
  "jwt": "eyJhbGc...",
  "expirationMinutes": 120,
  "expiresAt": "2026-02-16T19:30:00Z"
}
```

### Teste 2: Verificar Formato ISO 8601
```bash
# No PowerShell
$response = Invoke-WebRequest -Uri http://localhost:8080/auth/login `
  -Method POST `
  -Headers @{"Content-Type"="application/json"} `
  -Body '{"login":"seu_usuario","senha":"sua_senha"}' | ConvertFrom-Json

Write-Host "Token expira em: $($response.expirationMinutes) minutos"
Write-Host "Data/Hora: $($response.expiresAt)"
```

## Compatibilidade com Código Antigo

O construtor `DadosTokenJWT(String jwt)` foi mantido para compatibilidade com código que possa estar usando a classe anterior. Exemplos de uso em caso de erro:

```java
// Retornar apenas o jwt (compatível)
return ResponseEntity.badRequest().body(new DadosTokenJWT(e.getMessage()));

// Os campos expirationMinutes e expiresAt serão null neste caso
// JSON resultante: {"jwt": "mensagem de erro", "expirationMinutes": null, "expiresAt": null}
```

## Resumo

✅ Agora o método `efetuarLogin` retorna:
- Token JWT
- Tempo de expiração em minutos
- Data/hora de expiração em formato ISO 8601

✅ Pode ser usado no frontend para:
- Exibir contador de expiração
- Alertar usuário antes de expiração
- Redirecionar para login automaticamente

✅ Mantém compatibilidade com código antigo

