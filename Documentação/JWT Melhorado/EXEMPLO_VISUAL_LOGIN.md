# Exemplo Visual: Resposta do Login com Expiração

## 📊 Resposta JSON Completa do Método `efetuarLogin`

### Request
```http
POST /auth/login HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "login": "joao.silva@empresa.com",
  "senha": "senha123"
}
```

### Response (200 OK)
```json
{
  "jwt": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJBUEkgRmljaGEgVGVjbmljYSIsInN1YiI6ImpvYW8uc2lsdmFAZW1wcmVzYS5jb20iLCJyb2xlIjoiUk9MRV9VU0VSIiwibm9tZSI6Ikpvw6NvIFNpbHZhIiwiZXhwIjoxNzM5NzI1NjAwfQ.Z9x8Y7w6v5u4t3s2r1q0p9o8n7m6l5k4j3i2h1g0",
  "expirationMinutes": 120,
  "expiresAt": "2026-02-16T19:30:00Z"
}
```

### Decodificando o JWT
```json
HEADER:
{
  "alg": "HS256",
  "typ": "JWT"
}

PAYLOAD:
{
  "iss": "API Ficha Tecnica",
  "sub": "joao.silva@empresa.com",
  "role": "ROLE_USER",
  "nome": "João Silva",
  "exp": 1739725600
}

SIGNATURE:
Z9x8Y7w6v5u4t3s2r1q0p9o8n7m6l5k4j3i2h1g0
```

---

## 🎯 Exemplos com Diferentes Configurações

### Cenário 1: Web App (15 minutos)
```env
JWT_EXPIRATION_MINUTES=15
```

**Response:**
```json
{
  "jwt": "eyJhbGciOiJIUzI1NiI...",
  "expirationMinutes": 15,
  "expiresAt": "2026-02-16T16:45:00Z"
}
```

**Timeline:**
```
Agora: 16:30
Expira: 16:45 (+15 minutos)
```

---

### Cenário 2: Mobile App (1 hora)
```env
JWT_EXPIRATION_MINUTES=60
```

**Response:**
```json
{
  "jwt": "eyJhbGciOiJIUzI1NiI...",
  "expirationMinutes": 60,
  "expiresAt": "2026-02-16T17:30:00Z"
}
```

**Timeline:**
```
Agora: 16:30
Expira: 17:30 (+1 hora)
```

---

### Cenário 3: Sistema Administrativo (4 horas)
```env
JWT_EXPIRATION_MINUTES=240
```

**Response:**
```json
{
  "jwt": "eyJhbGciOiJIUzI1NiI...",
  "expirationMinutes": 240,
  "expiresAt": "2026-02-16T20:30:00Z"
}
```

**Timeline:**
```
Agora: 16:30
Expira: 20:30 (+4 horas)
```

---

### Cenário 4: Padrão (2 horas - sem variável definida)
```env
# JWT_EXPIRATION_MINUTES não definida (usa default 120)
```

**Response:**
```json
{
  "jwt": "eyJhbGciOiJIUzI1NiI...",
  "expirationMinutes": 120,
  "expiresAt": "2026-02-16T18:30:00Z"
}
```

**Timeline:**
```
Agora: 16:30
Expira: 18:30 (+2 horas)
```

---

## 🔄 Fluxo Completo do Login e Expiração

```
┌─────────────────────────────────────────────────────────────┐
│                    FLUXO DE LOGIN E EXPIRAÇÃO                 │
└─────────────────────────────────────────────────────────────┘

1. CLIENTE FAZ LOGIN
   ┌──────────────────────────────┐
   │ POST /auth/login             │
   │ {"login": "user@mail.com",   │
   │  "senha": "senha123"}        │
   └──────────────────────────────┘
                 │
                 ▼
2. SERVIDOR AUTENTICA
   ┌──────────────────────────────┐
   │ - Valida credenciais         │
   │ - Gera JWT Token             │
   │ - Calcula data de expiração  │
   └──────────────────────────────┘
                 │
                 ▼
3. RESPOSTA COM EXPIRAÇÃO
   ┌──────────────────────────────┐
   │ {                            │
   │   "jwt": "token...",         │
   │   "expirationMinutes": 120,  │
   │   "expiresAt": "2026-02-..." │
   │ }                            │
   └──────────────────────────────┘
                 │
                 ▼
4. CLIENTE ARMAZENA DADOS
   ┌──────────────────────────────┐
   │ localStorage.setItem("jwt", ...) │
   │ localStorage.setItem("expiresAt", ...) │
   └──────────────────────────────┘
                 │
                 ▼
5. CLIENTE FAZA REQUISIÇÃO COM TOKEN
   ┌──────────────────────────────┐
   │ GET /api/items               │
   │ Headers: {                   │
   │   "Authorization": "Bearer .."}│
   │ }                            │
   └──────────────────────────────┘
                 │
                 ▼
   ┌─────────────────────────────────────┐
   │ TOKEN EXPIRADO?                     │
   └─────────────────────────────────────┘
        │                        │
        NO                       SIM
        │                        │
        ▼                        ▼
   ┌──────────────┐     ┌──────────────────────┐
   │ Processa     │     │ Retorna 401          │
   │ Requisição   │     │ "Token expirado"     │
   │              │     │                      │
   │ Retorna 200  │     │ Cliente limpa dados  │
   │ COM DADOS    │     │ Redireciona para     │
   └──────────────┘     │ login                │
                        └──────────────────────┘
```

---

## 📱 Implementação no Frontend (React)

### 1. Componente de Login
```tsx
import React, { useState } from 'react';

interface LoginResponse {
  jwt: string;
  expirationMinutes: number;
  expiresAt: string;
}

export function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleLogin = async () => {
    setLoading(true);
    try {
      const response = await fetch('/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          login: username,
          senha: password
        })
      });

      const data: LoginResponse = await response.json();

      if (response.ok && data.jwt) {
        // Armazenar dados de autenticação
        localStorage.setItem('jwtToken', data.jwt);
        localStorage.setItem('expirationMinutes', data.expirationMinutes.toString());
        localStorage.setItem('expiresAt', data.expiresAt);

        // Mostrar informações
        alert(
          `✅ Login realizado com sucesso!\n\n` +
          `Token expira em: ${data.expirationMinutes} minutos\n` +
          `Às: ${new Date(data.expiresAt).toLocaleString()}`
        );

        // Redirecionar para dashboard
        window.location.href = '/dashboard';
      } else {
        setError('Credenciais inválidas');
      }
    } catch (err) {
      setError('Erro ao fazer login');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <h2>Login</h2>
      <input
        type="email"
        placeholder="E-mail"
        value={username}
        onChange={(e) => setUsername(e.target.value)}
      />
      <input
        type="password"
        placeholder="Senha"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
      />
      <button onClick={handleLogin} disabled={loading}>
        {loading ? 'Entrando...' : 'Login'}
      </button>
      {error && <p style={{ color: 'red' }}>{error}</p>}
    </div>
  );
}
```

### 2. Componente de Sessão
```tsx
import React, { useEffect, useState } from 'react';

export function SessionTimer() {
  const [timeLeft, setTimeLeft] = useState<string | null>(null);
  const [isExpiringSoon, setIsExpiringSoon] = useState(false);

  useEffect(() => {
    const expiresAt = localStorage.getItem('expiresAt');
    if (!expiresAt) return;

    const interval = setInterval(() => {
      const expires = new Date(expiresAt);
      const now = new Date();
      const diff = expires.getTime() - now.getTime();

      if (diff <= 0) {
        // Token expirou
        localStorage.clear();
        alert('Sua sessão expirou. Faça login novamente.');
        window.location.href = '/login';
      } else {
        const minutes = Math.floor(diff / (1000 * 60));
        const seconds = Math.floor((diff % (1000 * 60)) / 1000);
        setTimeLeft(`${minutes}:${seconds.toString().padStart(2, '0')}`);
        setIsExpiringSoon(minutes <= 5);
      }
    }, 1000);

    return () => clearInterval(interval);
  }, []);

  if (!timeLeft) return null;

  return (
    <div
      style={{
        padding: '10px 20px',
        margin: '10px 0',
        borderRadius: '4px',
        backgroundColor: isExpiringSoon ? '#ff6b6b' : '#51cf66',
        color: 'white',
        textAlign: 'center',
        fontWeight: 'bold'
      }}
    >
      {isExpiringSoon && '⚠️ '}
      Sessão expira em: {timeLeft}
    </div>
  );
}

// Usar no layout principal
export function Dashboard() {
  return (
    <div>
      <SessionTimer />
      {/* ... resto do dashboard ... */}
    </div>
  );
}
```

---

## 🧪 Testando com cURL

### Teste 1: Login e Capturar Resposta
```bash
# Windows PowerShell
$response = Invoke-RestMethod -Uri "http://localhost:8080/auth/login" `
  -Method POST `
  -Headers @{"Content-Type"="application/json"} `
  -Body '{"login":"usuario@email.com","senha":"senha123"}'

Write-Host "=== Resposta do Login ===" -ForegroundColor Green
Write-Host "Token: $($response.jwt.Substring(0, 50))..." -ForegroundColor Cyan
Write-Host "Expira em: $($response.expirationMinutes) minutos" -ForegroundColor Yellow
Write-Host "Às: $($response.expiresAt)" -ForegroundColor Magenta
```

### Teste 2: Usar Token em Requisição
```bash
# PowerShell
$token = "seu_jwt_token_aqui"

$response = Invoke-RestMethod -Uri "http://localhost:8080/api/items" `
  -Method GET `
  -Headers @{
    "Authorization"="Bearer $token"
    "Content-Type"="application/json"
  }

Write-Host $response | ConvertTo-Json
```

### Teste 3: Testar Expiração (com 1 minuto)
```bash
# 1. Definir JWT_EXPIRATION_MINUTES=1
[System.Environment]::SetEnvironmentVariable("JWT_EXPIRATION_MINUTES", "1", "Machine")

# 2. Reiniciar a aplicação

# 3. Fazer login e obter token
$response = Invoke-RestMethod -Uri "http://localhost:8080/auth/login" `
  -Method POST `
  -Headers @{"Content-Type"="application/json"} `
  -Body '{"login":"usuario@email.com","senha":"senha123"}'

$token = $response.jwt
Write-Host "Token obtido. Aguardando 65 segundos..."

# 4. Aguardar 65 segundos
Start-Sleep -Seconds 65

# 5. Tentar usar o token (deve retornar 401)
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/items" `
  -Method GET `
  -Headers @{"Authorization"="Bearer $token"} `
  -ErrorAction SilentlyContinue

Write-Host "Status: $($response.StatusCode)"
# Esperado: 401 Unauthorized
```

---

## 📊 Visualização de Dados na Resposta

```
┌─────────────────────────────────────────────────────────────┐
│              ESTRUTURA DA RESPOSTA JSON                      │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  {                                                           │
│    "jwt": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",       │
│             ▲                                                │
│             │                                                │
│             └─ Token JWT para uso em requisições             │
│                                                              │
│    "expirationMinutes": 120,                                │
│                          ▲                                   │
│                          │                                   │
│                          └─ Tempo de expiração em minutos    │
│                                                              │
│    "expiresAt": "2026-02-16T19:30:00Z"                      │
│                  ▲                                           │
│                  │                                           │
│                  └─ Data/hora de expiração (ISO 8601)        │
│                     Pode ser convertida para hora local      │
│  }                                                           │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## ✅ Checklist de Implementação

- [x] `DadosTokenJWT` atualizado com campos de expiração
- [x] `TokenService` com métodos `getExpirationMinutes()` e `getTokenExpiresAt()`
- [x] `AutenticacaoController.efetuarLogin()` retorna dados de expiração
- [x] Compatibilidade com código antigo mantida
- [x] Configuração via variável de sistema `JWT_EXPIRATION_MINUTES`
- [x] Documentação completa
- [x] Exemplos de uso no frontend
- [x] Exemplos de testes com cURL

---

## 🚀 Próximos Passos

1. **Implementar no Frontend**: Use os exemplos acima para mostrar contador de expiração
2. **Refresh Token**: Implementar endpoint para renovar token sem fazer login novamente
3. **Logout**: Implementar logout que limpe o token no frontend
4. **Auditoria**: Registrar logins e tentativas de acesso com token expirado

---

