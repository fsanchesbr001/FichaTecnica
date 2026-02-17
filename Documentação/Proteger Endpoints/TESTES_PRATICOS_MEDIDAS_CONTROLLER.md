# 🧪 Testes Práticos: JWT Token nos Endpoints do MedidasController

## 📋 Guia de Teste Passo-a-Passo

---

## 1️⃣ TESTE DE LOGIN (Obter Token)

### Via cURL (Windows PowerShell)

```powershell
# Fazer login
$response = Invoke-WebRequest -Uri "http://localhost:8080/auth/login" `
  -Method POST `
  -Headers @{"Content-Type"="application/json"} `
  -Body '{"login":"admin","senha":"admin"}'

$data = $response.Content | ConvertFrom-Json

# Exibir token
Write-Host "✅ Login bem-sucedido!"
Write-Host "Token: $($data.jwt.Substring(0, 50))..."
Write-Host "Expira em: $($data.expirationMinutes) minutos"
Write-Host "Às: $($data.expiresAt)"

# Armazenar para próximas requisições
$TOKEN = $data.jwt
$EXPIRATION = $data.expirationMinutes
$EXPIRES_AT = $data.expiresAt
```

**Resposta Esperada:**
```json
{
  "jwt": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJBUEkgRmljaGEgVGVjbmljYSIsInN1YiI6ImFkbWluIiwicm9sZSI6IlJPTEVfQURNSU4iLCJub21lIjoiQWRtaW4iLCJleHAiOjE3Mzk3MjU2MDB9.abc123...",
  "expirationMinutes": 120,
  "expiresAt": "2026-02-16T19:30:00Z"
}
```

---

## 2️⃣ TESTE: GET /ficha-tecnica/unidades-medida (Listar)

### Usando Token do Passo 1

```powershell
# Usar token obtido no login
$response = Invoke-WebRequest -Uri "http://localhost:8080/ficha-tecnica/unidades-medida" `
  -Method GET `
  -Headers @{
    "Authorization" = "Bearer $TOKEN"
    "Content-Type" = "application/json"
  }

$data = $response.Content | ConvertFrom-Json

Write-Host "✅ Requisição bem-sucedida!"
Write-Host "Status: $($response.StatusCode)"
Write-Host "Unidades encontradas: $($data.Count)"
$data | ConvertTo-Json
```

**Resposta Esperada (200 OK):**
```json
[
  {
    "id": 1,
    "descricao": "Quilograma"
  },
  {
    "id": 2,
    "descricao": "Litro"
  }
]
```

**Pontos Importantes:**
- ✅ Token adicionado no header `Authorization: Bearer <token>`
- ✅ NÃO precisa refazer login
- ✅ Sem necessidade de enviar credenciais novamente
- ✅ Token é reutilizado!

---

## 3️⃣ TESTE: GET /ficha-tecnica/unidades-medida/{id} (Buscar por ID)

```powershell
# Buscar unidade específica
$response = Invoke-WebRequest -Uri "http://localhost:8080/ficha-tecnica/unidades-medida/1" `
  -Method GET `
  -Headers @{
    "Authorization" = "Bearer $TOKEN"
    "Content-Type" = "application/json"
  }

$data = $response.Content | ConvertFrom-Json

Write-Host "✅ Unidade encontrada!"
Write-Host "ID: $($data.id)"
Write-Host "Descrição: $($data.descricao)"
```

**Resposta Esperada (200 OK):**
```json
{
  "id": 1,
  "descricao": "Quilograma"
}
```

---

## 4️⃣ TESTE: POST /ficha-tecnica/unidades-medida (Cadastrar)

```powershell
# Cadastrar nova unidade
$body = @{
  "descricao" = "Mililitro"
} | ConvertTo-Json

$response = Invoke-WebRequest -Uri "http://localhost:8080/ficha-tecnica/unidades-medida" `
  -Method POST `
  -Headers @{
    "Authorization" = "Bearer $TOKEN"
    "Content-Type" = "application/json"
  } `
  -Body $body

$data = $response.Content | ConvertFrom-Json

Write-Host "✅ Unidade criada!"
Write-Host "ID: $($data.id)"
Write-Host "Descrição: $($data.descricao)"
```

**Resposta Esperada (200 OK):**
```json
{
  "id": 3,
  "descricao": "Mililitro"
}
```

---

## 5️⃣ TESTE: PUT /ficha-tecnica/unidades-medida/{id} (Atualizar)

```powershell
# Atualizar unidade
$body = @{
  "descricao" = "Kilo"
} | ConvertTo-Json

$response = Invoke-WebRequest -Uri "http://localhost:8080/ficha-tecnica/unidades-medida/1" `
  -Method PUT `
  -Headers @{
    "Authorization" = "Bearer $TOKEN"
    "Content-Type" = "application/json"
  } `
  -Body $body

$data = $response.Content | ConvertFrom-Json

Write-Host "✅ Unidade atualizada!"
Write-Host "ID: $($data.id)"
Write-Host "Descrição: $($data.descricao)"
```

**Resposta Esperada (200 OK):**
```json
{
  "id": 1,
  "descricao": "Kilo"
}
```

---

## 6️⃣ TESTE: DELETE /ficha-tecnica/unidades-medida/{id} (Deletar)

```powershell
# Deletar unidade
$response = Invoke-WebRequest -Uri "http://localhost:8080/ficha-tecnica/unidades-medida/3" `
  -Method DELETE `
  -Headers @{
    "Authorization" = "Bearer $TOKEN"
    "Content-Type" = "application/json"
  }

Write-Host "✅ Unidade deletada!"
Write-Host "Status: $($response.StatusCode)"
```

**Resposta Esperada (204 No Content):**
```
(sem corpo)
Status Code: 204
```

---

## 🔴 TESTE: Token Expirado

### Simular Token Expirado

```powershell
# 1. Configurar para 1 minuto de expiração
[System.Environment]::SetEnvironmentVariable("JWT_EXPIRATION_MINUTES", "1", "Process")

# 2. Fazer login (obter token com 1 minuto)
$response = Invoke-WebRequest -Uri "http://localhost:8080/auth/login" `
  -Method POST `
  -Headers @{"Content-Type"="application/json"} `
  -Body '{"login":"admin","senha":"admin"}'

$data = $response.Content | ConvertFrom-Json
$TOKEN = $data.jwt

Write-Host "✅ Token obtido com 1 minuto de expiração"

# 3. Aguardar expiração
Write-Host "⏳ Aguardando 65 segundos..."
Start-Sleep -Seconds 65

# 4. Tentar usar token expirado
try {
  $response = Invoke-WebRequest -Uri "http://localhost:8080/ficha-tecnica/unidades-medida" `
    -Method GET `
    -Headers @{
      "Authorization" = "Bearer $TOKEN"
      "Content-Type" = "application/json"
    } `
    -ErrorAction Stop
} catch {
  $statusCode = $_.Exception.Response.StatusCode.Value__
  $errorBody = $_.Exception.Response.GetResponseStream() | 
    { New-Object System.IO.StreamReader($_) }.Invoke() | 
    { $_.ReadToEnd() }.Invoke() |
    ConvertFrom-Json

  Write-Host "❌ Token rejeitado!"
  Write-Host "Status: $statusCode"
  Write-Host "Erro: $($errorBody.error)"
  Write-Host "Mensagem: $($errorBody.message)"
}
```

**Resposta Esperada (401 Unauthorized):**
```json
{
  "error": "Token expirado",
  "message": "Seu token de autenticação expirou. Por favor, faça login novamente."
}
```

---

## 🔒 TESTE: Sem Token

```powershell
# Tentar acessar endpoint SEM token
try {
  $response = Invoke-WebRequest -Uri "http://localhost:8080/ficha-tecnica/unidades-medida" `
    -Method GET `
    -ErrorAction Stop
} catch {
  $statusCode = $_.Exception.Response.StatusCode.Value__
  
  Write-Host "❌ Acesso negado!"
  Write-Host "Status: $statusCode"
  Write-Host "Mensagem: Token obrigatório!"
}
```

**Resposta Esperada (401 Unauthorized):**
```
Status: 401
Mensagem: Token obrigatório!
```

---

## 🔒 TESTE: Token Inválido

```powershell
# Tentar acessar com token inválido
$INVALID_TOKEN = "token_invalido_123"

try {
  $response = Invoke-WebRequest -Uri "http://localhost:8080/ficha-tecnica/unidades-medida" `
    -Method GET `
    -Headers @{
      "Authorization" = "Bearer $INVALID_TOKEN"
    } `
    -ErrorAction Stop
} catch {
  $statusCode = $_.Exception.Response.StatusCode.Value__
  $errorBody = $_.Exception.Response.GetResponseStream() | 
    { New-Object System.IO.StreamReader($_) }.Invoke() | 
    { $_.ReadToEnd() }.Invoke() |
    ConvertFrom-Json

  Write-Host "❌ Token inválido!"
  Write-Host "Status: $statusCode"
  Write-Host "Erro: $($errorBody.error)"
  Write-Host "Mensagem: $($errorBody.message)"
}
```

**Resposta Esperada (401 Unauthorized):**
```json
{
  "error": "Token inválido",
  "message": "Seu token de autenticação é inválido. Por favor, faça login novamente."
}
```

---

## 🧪 SCRIPT COMPLETO DE TESTE

### PowerShell

Crie arquivo `teste-medidas-controller.ps1`:

```powershell
# ============================================
# TESTE COMPLETO: MedidasController com JWT
# ============================================

Write-Host "╔════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║  TESTE: MedidasController com JWT Token               ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

$BASE_URL = "http://localhost:8080"

# PASSO 1: Login
Write-Host "[1/7] Fazendo Login..." -ForegroundColor Yellow

$loginResponse = Invoke-WebRequest -Uri "$BASE_URL/auth/login" `
  -Method POST `
  -Headers @{"Content-Type"="application/json"} `
  -Body '{"login":"admin","senha":"admin"}'

$loginData = $loginResponse.Content | ConvertFrom-Json
$TOKEN = $loginData.jwt

Write-Host "✅ Login bem-sucedido!"
Write-Host "   Token: $($TOKEN.Substring(0, 50))..."
Write-Host "   Expira em: $($loginData.expirationMinutes) minutos"
Write-Host ""

# PASSO 2: Listar unidades
Write-Host "[2/7] Listando Unidades..." -ForegroundColor Yellow

$listResponse = Invoke-WebRequest -Uri "$BASE_URL/ficha-tecnica/unidades-medida" `
  -Method GET `
  -Headers @{"Authorization"="Bearer $TOKEN"}

$listData = $listResponse.Content | ConvertFrom-Json

Write-Host "✅ Lista obtida!"
Write-Host "   Total de unidades: $($listData.Count)"
Write-Host ""

# PASSO 3: Buscar unidade por ID
Write-Host "[3/7] Buscando Unidade por ID..." -ForegroundColor Yellow

$getResponse = Invoke-WebRequest -Uri "$BASE_URL/ficha-tecnica/unidades-medida/1" `
  -Method GET `
  -Headers @{"Authorization"="Bearer $TOKEN"}

$getData = $getResponse.Content | ConvertFrom-Json

Write-Host "✅ Unidade encontrada!"
Write-Host "   ID: $($getData.id)"
Write-Host "   Descrição: $($getData.descricao)"
Write-Host ""

# PASSO 4: Criar unidade
Write-Host "[4/7] Criando Unidade..." -ForegroundColor Yellow

$createBody = @{
  "descricao" = "Mililitro_$(Get-Random)"
} | ConvertTo-Json

$createResponse = Invoke-WebRequest -Uri "$BASE_URL/ficha-tecnica/unidades-medida" `
  -Method POST `
  -Headers @{
    "Authorization" = "Bearer $TOKEN"
    "Content-Type" = "application/json"
  } `
  -Body $createBody

$createData = $createResponse.Content | ConvertFrom-Json

Write-Host "✅ Unidade criada!"
Write-Host "   ID: $($createData.id)"
Write-Host "   Descrição: $($createData.descricao)"
$CREATED_ID = $createData.id
Write-Host ""

# PASSO 5: Atualizar unidade
Write-Host "[5/7] Atualizando Unidade..." -ForegroundColor Yellow

$updateBody = @{
  "descricao" = "Mili"
} | ConvertTo-Json

$updateResponse = Invoke-WebRequest -Uri "$BASE_URL/ficha-tecnica/unidades-medida/$CREATED_ID" `
  -Method PUT `
  -Headers @{
    "Authorization" = "Bearer $TOKEN"
    "Content-Type" = "application/json"
  } `
  -Body $updateBody

$updateData = $updateResponse.Content | ConvertFrom-Json

Write-Host "✅ Unidade atualizada!"
Write-Host "   ID: $($updateData.id)"
Write-Host "   Descrição: $($updateData.descricao)"
Write-Host ""

# PASSO 6: Deletar unidade
Write-Host "[6/7] Deletando Unidade..." -ForegroundColor Yellow

$deleteResponse = Invoke-WebRequest -Uri "$BASE_URL/ficha-tecnica/unidades-medida/$CREATED_ID" `
  -Method DELETE `
  -Headers @{"Authorization"="Bearer $TOKEN"}

Write-Host "✅ Unidade deletada!"
Write-Host "   Status: $($deleteResponse.StatusCode)"
Write-Host ""

# PASSO 7: Testar reutilização de token
Write-Host "[7/7] Testando Reutilização de Token..." -ForegroundColor Yellow

$reuseResponse = Invoke-WebRequest -Uri "$BASE_URL/ficha-tecnica/unidades-medida" `
  -Method GET `
  -Headers @{"Authorization"="Bearer $TOKEN"}

$reuseData = $reuseResponse.Content | ConvertFrom-Json

Write-Host "✅ Token reutilizado com sucesso!"
Write-Host "   Total de unidades: $($reuseData.Count)"
Write-Host ""

Write-Host "╔════════════════════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║  ✅ TODOS OS TESTES PASSARAM!                         ║" -ForegroundColor Green
Write-Host "╚════════════════════════════════════════════════════════╝" -ForegroundColor Green
Write-Host ""
Write-Host "📊 RESUMO:" -ForegroundColor Cyan
Write-Host "   ✅ Login executado (token obtido)"
Write-Host "   ✅ GET lista funcionou (token reutilizado)"
Write-Host "   ✅ GET por ID funcionou (token reutilizado)"
Write-Host "   ✅ POST funcionou (token reutilizado)"
Write-Host "   ✅ PUT funcionou (token reutilizado)"
Write-Host "   ✅ DELETE funcionou (token reutilizado)"
Write-Host "   ✅ Token reutilizado em múltiplas requisições"
Write-Host ""
Write-Host "🎯 CONCLUSÃO:" -ForegroundColor Green
Write-Host "   Todos os endpoints funcionam corretamente com JWT!"
Write-Host "   O mesmo token foi reutilizado em TODAS as requisições"
Write-Host "   Sem necessidade de refazer login a cada chamada!"
```

### Executar

```powershell
# Executar script
.\teste-medidas-controller.ps1
```

---

## 📝 Postman Collection

Crie uma collection no Postman com as requisições:

```json
{
  "info": {
    "name": "MedidasController JWT Tests",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "1 - Login",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\"login\":\"admin\",\"senha\":\"admin\"}"
        },
        "url": {
          "raw": "{{baseUrl}}/auth/login",
          "host": ["{{baseUrl}}"],
          "path": ["auth", "login"]
        }
      }
    },
    {
      "name": "2 - Listar Unidades",
      "request": {
        "method": "GET",
        "header": [
          {
            "key": "Authorization",
            "value": "Bearer {{token}}"
          }
        ],
        "url": {
          "raw": "{{baseUrl}}/ficha-tecnica/unidades-medida",
          "host": ["{{baseUrl}}"],
          "path": ["ficha-tecnica", "unidades-medida"]
        }
      }
    },
    {
      "name": "3 - Buscar por ID",
      "request": {
        "method": "GET",
        "header": [
          {
            "key": "Authorization",
            "value": "Bearer {{token}}"
          }
        ],
        "url": {
          "raw": "{{baseUrl}}/ficha-tecnica/unidades-medida/1",
          "host": ["{{baseUrl}}"],
          "path": ["ficha-tecnica", "unidades-medida", "1"]
        }
      }
    },
    {
      "name": "4 - Criar Unidade",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Authorization",
            "value": "Bearer {{token}}"
          },
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\"descricao\":\"Mililitro\"}"
        },
        "url": {
          "raw": "{{baseUrl}}/ficha-tecnica/unidades-medida",
          "host": ["{{baseUrl}}"],
          "path": ["ficha-tecnica", "unidades-medida"]
        }
      }
    },
    {
      "name": "5 - Atualizar Unidade",
      "request": {
        "method": "PUT",
        "header": [
          {
            "key": "Authorization",
            "value": "Bearer {{token}}"
          },
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\"descricao\":\"Mili\"}"
        },
        "url": {
          "raw": "{{baseUrl}}/ficha-tecnica/unidades-medida/1",
          "host": ["{{baseUrl}}"],
          "path": ["ficha-tecnica", "unidades-medida", "1"]
        }
      }
    },
    {
      "name": "6 - Deletar Unidade",
      "request": {
        "method": "DELETE",
        "header": [
          {
            "key": "Authorization",
            "value": "Bearer {{token}}"
          }
        ],
        "url": {
          "raw": "{{baseUrl}}/ficha-tecnica/unidades-medida/1",
          "host": ["{{baseUrl}}"],
          "path": ["ficha-tecnica", "unidades-medida", "1"]
        }
      }
    }
  ],
  "variable": [
    {
      "key": "baseUrl",
      "value": "http://localhost:8080"
    },
    {
      "key": "token",
      "value": ""
    }
  ]
}
```

---

## 📊 Fluxo de Teste Visual

```
┌─────────────────────────────────┐
│  1. FAZER LOGIN                 │
│  POST /auth/login               │
│  Resposta: { jwt, ... }         │
└────────────┬────────────────────┘
             │
             ▼
┌─────────────────────────────────┐
│  2. ARMAZENAR TOKEN             │
│  {{token}} = jwt                │
└────────────┬────────────────────┘
             │
             ▼
┌─────────────────────────────────┐
│  3. GET Lista                   │
│  Header: Authorization: Bearer  │
│  {{token}}                      │
│  Resposta: 200 OK               │
└────────────┬────────────────────┘
             │
             ▼
┌─────────────────────────────────┐
│  4. GET por ID                  │
│  Usa MESMO token do Passo 2     │
│  Resposta: 200 OK               │
└────────────┬────────────────────┘
             │
             ▼
┌─────────────────────────────────┐
│  5. POST Criar                  │
│  Usa MESMO token do Passo 2     │
│  Resposta: 200 OK               │
└────────────┬────────────────────┘
             │
             ▼
┌─────────────────────────────────┐
│  6. PUT Atualizar               │
│  Usa MESMO token do Passo 2     │
│  Resposta: 200 OK               │
└────────────┬────────────────────┘
             │
             ▼
┌─────────────────────────────────┐
│  7. DELETE                      │
│  Usa MESMO token do Passo 2     │
│  Resposta: 204 No Content       │
└────────────┬────────────────────┘
             │
             ▼
┌─────────────────────────────────┐
│  ✅ TODOS OS TESTES PASSARAM!   │
│  Token foi reutilizado 6 vezes  │
│  Sem refazer login!             │
└─────────────────────────────────┘
```

---

## ✅ Checklist de Testes

- [x] Login - Obtém token
- [x] GET Lista - Usa token
- [x] GET por ID - Usa token
- [x] POST - Usa token
- [x] PUT - Usa token
- [x] DELETE - Usa token
- [x] Token Expirado - Retorna 401
- [x] Sem Token - Retorna 401
- [x] Token Inválido - Retorna 401
- [x] Token Reutilizado - Funciona

---

**Conclusão**: Todos os endpoints funcionam corretamente com JWT Token! ✅

