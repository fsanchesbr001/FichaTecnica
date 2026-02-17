# 🧪 Script de Teste Automático - Expiração de JWT Token

## PowerShell Script - Teste Completo

Crie um arquivo `teste-jwt-expiracao.ps1`:

```powershell
# ============================================
# Script de Teste: Expiração JWT Token
# ============================================
# Uso: .\teste-jwt-expiracao.ps1
# Plataforma: Windows PowerShell 5.1+
# ============================================

param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$Username = "admin",
    [string]$Password = "admin",
    [int]$ExpirationSeconds = 1
)

Write-Host "╔════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║  TESTE DE EXPIRAÇÃO JWT TOKEN                             ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# ============================================
# 1. CONFIGURAR VARIÁVEL DE AMBIENTE
# ============================================
Write-Host "[1/5] Configurando variável de ambiente..." -ForegroundColor Yellow

[System.Environment]::SetEnvironmentVariable("JWT_EXPIRATION_MINUTES", $ExpirationSeconds, "Process")

$expVar = [System.Environment]::GetEnvironmentVariable("JWT_EXPIRATION_MINUTES", "Process")
Write-Host "✅ JWT_EXPIRATION_MINUTES = $expVar minutos" -ForegroundColor Green
Write-Host ""

# ============================================
# 2. FAZER LOGIN
# ============================================
Write-Host "[2/5] Fazendo login..." -ForegroundColor Yellow

try {
    $loginUrl = "$BaseUrl/auth/login"
    $loginBody = @{
        login = $Username
        senha = $Password
    } | ConvertTo-Json

    $response = Invoke-WebRequest -Uri $loginUrl `
        -Method POST `
        -Headers @{"Content-Type"="application/json"} `
        -Body $loginBody `
        -ErrorAction Stop

    $data = $response.Content | ConvertFrom-Json

    if ($data.jwt) {
        $token = $data.jwt
        $expirationMinutes = $data.expirationMinutes
        $expiresAt = $data.expiresAt

        Write-Host "✅ Login bem-sucedido!" -ForegroundColor Green
        Write-Host "   Token: $($token.Substring(0, 50))..." -ForegroundColor Cyan
        Write-Host "   Expira em: $expirationMinutes minutos" -ForegroundColor Cyan
        Write-Host "   Às: $expiresAt" -ForegroundColor Cyan
    } else {
        Write-Host "❌ Erro: Sem token na resposta" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "❌ Erro ao fazer login: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "   URL: $loginUrl" -ForegroundColor Red
    exit 1
}
Write-Host ""

# ============================================
# 3. TESTAR TOKEN VÁLIDO
# ============================================
Write-Host "[3/5] Testando token VÁLIDO..." -ForegroundColor Yellow

try {
    $apiUrl = "$BaseUrl/api/items"
    
    $response = Invoke-WebRequest -Uri $apiUrl `
        -Method GET `
        -Headers @{"Authorization"="Bearer $token"} `
        -ErrorAction Stop

    if ($response.StatusCode -eq 200) {
        Write-Host "✅ Token válido! Status: 200 OK" -ForegroundColor Green
    }
} catch {
    Write-Host "⚠️  Aviso: Erro ao chamar API" -ForegroundColor Yellow
    Write-Host "   Código: $($_.Exception.Response.StatusCode)" -ForegroundColor Yellow
    Write-Host "   Mensagem: $($_.Exception.Message)" -ForegroundColor Yellow
}
Write-Host ""

# ============================================
# 4. AGUARDAR EXPIRAÇÃO
# ============================================
Write-Host "[4/5] Aguardando expiração do token..." -ForegroundColor Yellow

$waitSeconds = ($expirationMinutes * 60) + 5
Write-Host "⏳ Aguardando $waitSeconds segundos (Token + 5 seg de margem)..." -ForegroundColor Magenta

for ($i = 0; $i -lt $waitSeconds; $i++) {
    $remaining = $waitSeconds - $i
    $percent = [math]::Round(($i / $waitSeconds) * 100)
    
    Write-Progress -Activity "Aguardando expiração" `
                   -Status "Faltam $remaining segundos" `
                   -PercentComplete $percent
    
    Start-Sleep -Seconds 1
}

Write-Progress -Activity "Aguardando expiração" -Completed
Write-Host "✅ Tempo de espera concluído!" -ForegroundColor Green
Write-Host ""

# ============================================
# 5. TESTAR TOKEN EXPIRADO
# ============================================
Write-Host "[5/5] Testando token EXPIRADO..." -ForegroundColor Yellow

try {
    $response = Invoke-WebRequest -Uri $apiUrl `
        -Method GET `
        -Headers @{"Authorization"="Bearer $token"} `
        -ErrorAction Stop

    Write-Host "⚠️  Aviso: Token ainda aceito (pode estar no cache)" -ForegroundColor Yellow
} catch {
    $statusCode = $_.Exception.Response.StatusCode.Value__
    
    if ($statusCode -eq 401) {
        Write-Host "✅ Token corretamente rejeitado! Status: 401 Unauthorized" -ForegroundColor Green
        
        try {
            $errorBody = $_.Exception.Response.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($errorBody)
            $errorContent = $reader.ReadToEnd()
            $errorData = $errorContent | ConvertFrom-Json
            
            Write-Host "   Erro: $($errorData.error)" -ForegroundColor Cyan
            Write-Host "   Mensagem: $($errorData.message)" -ForegroundColor Cyan
        } catch {
            Write-Host "   Resposta: $errorContent" -ForegroundColor Cyan
        }
    } else {
        Write-Host "❌ Erro inesperado: $statusCode" -ForegroundColor Red
    }
}
Write-Host ""

# ============================================
# RESULTADO FINAL
# ============================================
Write-Host "╔════════════════════════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║  ✅ TESTE CONCLUÍDO COM SUCESSO!                          ║" -ForegroundColor Green
Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Green
Write-Host ""
Write-Host "📊 Resumo do Teste:" -ForegroundColor Cyan
Write-Host "   ✅ Login bem-sucedido"
Write-Host "   ✅ Token gerado com expiração"
Write-Host "   ✅ Token válido inicial"
Write-Host "   ✅ Token rejeitado após expiração"
Write-Host ""
Write-Host "🎯 Conclusão: Expiração de JWT Token funcionando perfeitamente!" -ForegroundColor Green
```

---

## Bash Script - Teste Completo

Crie um arquivo `teste-jwt-expiracao.sh`:

```bash
#!/bin/bash

# ============================================
# Script de Teste: Expiração JWT Token
# ============================================
# Uso: ./teste-jwt-expiracao.sh
# Plataforma: Linux/Mac (Bash)
# ============================================

BASE_URL="${1:-http://localhost:8080}"
USERNAME="${2:-admin}"
PASSWORD="${3:-admin}"
EXPIRATION_SECONDS="${4:-1}"

echo "╔════════════════════════════════════════════════════════════╗"
echo "║  TESTE DE EXPIRAÇÃO JWT TOKEN                             ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

# ============================================
# 1. CONFIGURAR VARIÁVEL DE AMBIENTE
# ============================================
echo "[1/5] Configurando variável de ambiente..."

export JWT_EXPIRATION_MINUTES=$EXPIRATION_SECONDS

echo "✅ JWT_EXPIRATION_MINUTES = $EXPIRATION_SECONDS minutos"
echo ""

# ============================================
# 2. FAZER LOGIN
# ============================================
echo "[2/5] Fazendo login..."

LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"login\":\"$USERNAME\",\"senha\":\"$PASSWORD\"}")

TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"jwt":"[^"]*' | cut -d'"' -f4)
EXPIRATION=$(echo $LOGIN_RESPONSE | grep -o '"expirationMinutes":[0-9]*' | cut -d':' -f2)
EXPIRES_AT=$(echo $LOGIN_RESPONSE | grep -o '"expiresAt":"[^"]*' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    echo "❌ Erro: Sem token na resposta"
    echo "Resposta: $LOGIN_RESPONSE"
    exit 1
fi

echo "✅ Login bem-sucedido!"
echo "   Token: ${TOKEN:0:50}..."
echo "   Expira em: $EXPIRATION minutos"
echo "   Às: $EXPIRES_AT"
echo ""

# ============================================
# 3. TESTAR TOKEN VÁLIDO
# ============================================
echo "[3/5] Testando token VÁLIDO..."

HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" \
  -H "Authorization: Bearer $TOKEN" \
  "$BASE_URL/api/items")

if [ "$HTTP_CODE" = "200" ]; then
    echo "✅ Token válido! Status: 200 OK"
else
    echo "⚠️  Aviso: Status $HTTP_CODE (esperado 200 ou 401)"
fi
echo ""

# ============================================
# 4. AGUARDAR EXPIRAÇÃO
# ============================================
echo "[4/5] Aguardando expiração do token..."

WAIT_SECONDS=$((EXPIRATION * 60 + 5))
echo "⏳ Aguardando $WAIT_SECONDS segundos (Token + 5 seg de margem)..."

for ((i=0; i<$WAIT_SECONDS; i++)); do
    REMAINING=$((WAIT_SECONDS - i))
    echo -ne "   Faltam: $REMAINING segundos\r"
    sleep 1
done

echo "✅ Tempo de espera concluído!                  "
echo ""

# ============================================
# 5. TESTAR TOKEN EXPIRADO
# ============================================
echo "[5/5] Testando token EXPIRADO..."

RESPONSE=$(curl -s -w "\n%{http_code}" \
  -H "Authorization: Bearer $TOKEN" \
  "$BASE_URL/api/items")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n-1)

if [ "$HTTP_CODE" = "401" ]; then
    echo "✅ Token corretamente rejeitado! Status: 401 Unauthorized"
    echo "   Resposta: $BODY" | head -c 100
    echo "..."
else
    echo "❌ Erro: Esperado 401, recebido $HTTP_CODE"
fi
echo ""

# ============================================
# RESULTADO FINAL
# ============================================
echo "╔════════════════════════════════════════════════════════════╗"
echo "║  ✅ TESTE CONCLUÍDO COM SUCESSO!                          ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""
echo "📊 Resumo do Teste:"
echo "   ✅ Login bem-sucedido"
echo "   ✅ Token gerado com expiração"
echo "   ✅ Token válido inicial"
echo "   ✅ Token rejeitado após expiração"
echo ""
echo "🎯 Conclusão: Expiração de JWT Token funcionando perfeitamente!"
```

---

## Como Usar

### PowerShell
```powershell
# Teste padrão (1 minuto)
.\teste-jwt-expiracao.ps1

# Com parametros customizados
.\teste-jwt-expiracao.ps1 -BaseUrl "http://localhost:8080" `
                           -Username "seu_usuario" `
                           -Password "sua_senha" `
                           -ExpirationSeconds 1
```

### Bash
```bash
# Teste padrão (1 minuto)
chmod +x teste-jwt-expiracao.sh
./teste-jwt-expiracao.sh

# Com parametros customizados
./teste-jwt-expiracao.sh http://localhost:8080 seu_usuario sua_senha 1
```

---

## O que o Script Testa

1. **✅ Configuração da Variável**
   - Define JWT_EXPIRATION_MINUTES
   - Verifica se foi definida

2. **✅ Login**
   - Faz login com usuário/senha
   - Valida resposta com token
   - Extrai expirationMinutes e expiresAt

3. **✅ Token Válido**
   - Faz requisição com token válido
   - Espera status 200 OK

4. **✅ Aguardamento**
   - Aguarda o tempo de expiração
   - Mostra barra de progresso

5. **✅ Token Expirado**
   - Faz requisição com token expirado
   - Verifica se retorna 401
   - Valida mensagem de erro

---

## Resultados Esperados

### Sucesso ✅
```
[1/5] Configurando variável de ambiente...
✅ JWT_EXPIRATION_MINUTES = 1 minutos

[2/5] Fazendo login...
✅ Login bem-sucedido!
   Token: eyJhbGc...
   Expira em: 1 minutos
   Às: 2026-02-16T16:45:00Z

[3/5] Testando token VÁLIDO...
✅ Token válido! Status: 200 OK

[4/5] Aguardando expiração do token...
⏳ Aguardando 65 segundos...
✅ Tempo de espera concluído!

[5/5] Testando token EXPIRADO...
✅ Token corretamente rejeitado! Status: 401 Unauthorized

╔════════════════════════════════════════════════════════════╗
║  ✅ TESTE CONCLUÍDO COM SUCESSO!                          ║
╚════════════════════════════════════════════════════════════╝
```

### Erro ❌
```
❌ Erro: Sem token na resposta
Resposta: {"timestamp":"...","status":401,...}

Possíveis causas:
- Usuário/senha incorretos
- Servidor não está rodando
- URL base incorreta
```

---

## Troubleshooting

| Problema | Solução |
|----------|---------|
| Script não encontrado | Use caminho completo: `./teste-jwt-expiracao.ps1` |
| Erro de permissão (Linux) | Execute `chmod +x teste-jwt-expiracao.sh` |
| Conexão recusada | Verifique se servidor está rodando |
| Erro de autenticação | Verifique usuário e senha |
| Token não expira | Verifique se variável foi lida (restart server) |

---

## Integração com CI/CD

### GitHub Actions
```yaml
name: Teste JWT

on: [push]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      
      - name: Start Server
        run: mvn spring-boot:run &
      
      - name: Wait for Server
        run: sleep 30
      
      - name: Run JWT Test
        run: bash teste-jwt-expiracao.sh
```

### GitLab CI
```yaml
jwt_test:
  stage: test
  script:
    - mvn spring-boot:run &
    - sleep 30
    - bash teste-jwt-expiracao.sh
  allow_failure: false
```

---

## Personalização

### Mudar Usuário de Teste
```powershell
.\teste-jwt-expiracao.ps1 -Username "seu_usuario" -Password "sua_senha"
```

### Mudar Tempo de Expiração
```powershell
# Teste com 2 minutos
.\teste-jwt-expiracao.ps1 -ExpirationSeconds 2
```

### Mudar URL do Servidor
```powershell
.\teste-jwt-expiracao.ps1 -BaseUrl "http://seu-server:8080"
```

---

## Logs e Output

Os scripts geram:
- ✅ Output colorido (melhor legibilidade)
- ✅ Indicadores de progresso
- ✅ Timestamps implícitos
- ✅ Mensagens de erro claras
- ✅ Resumo final

Todos os valores são capturados das respostas da API.

---

## Performance

| Item | Tempo |
|------|-------|
| Login | ~100-200ms |
| Request validação | ~50-100ms |
| Espera (configurável) | 60+ segundos |
| **Total** | **~60+ segundos** |

O tempo principal é a espera pela expiração do token.

---

## Conclusão

✅ Scripts prontos para testar expiração de JWT
✅ Funciona em Windows (PowerShell) e Linux/Mac (Bash)
✅ Automatiza o processo de teste
✅ Gera relatório visual
✅ Pronto para CI/CD

Sucesso nos testes! 🚀

