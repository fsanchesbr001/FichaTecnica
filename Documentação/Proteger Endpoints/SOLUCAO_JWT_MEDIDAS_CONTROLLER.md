# ✅ SOLUÇÃO: JWT Token nos Endpoints do MedidasController

## 🎯 Suas Questões

### Questão 01
> "O login deve ser feito uma vez e deve retornar um token válido"

### Questão 02
> "Cada endpoint deve utilizar o token enquanto válido, sem necessidade de refazer o login a cada chamada e em caso de expiração retornar token expirado de acordo com fluxo existente"

---

## ✅ RESPOSTA: JÁ ESTÁ IMPLEMENTADO!

### Status Atual do Projeto

```
✅ BACKEND:
├─ SecurityConfigurations: Endpoints protegidos corretamente
├─ TokenService: Gera token com expiração
├─ SecurityFilter: Valida token em cada requisição
└─ MedidasController: Protegido automaticamente

✅ QUESTÃO 01: RESOLVIDA
├─ POST /auth/login retorna token
├─ Token com expiração em minutos
├─ Token com data/hora de expiração
└─ Tudo em uma única requisição

✅ QUESTÃO 02: RESOLVIDA
├─ SecurityFilter valida token automaticamente
├─ Token reutilizado em todas as requisições
├─ Sem necessidade de refazer login
├─ Retorna 401 quando token expira
└─ Mensagem clara de expiração
```

---

## 🚀 FLUXO PRÁTICO

### Passo 1: Login (UMA VEZ!)

```bash
# 1️⃣ Cliente faz login
POST http://localhost:8080/auth/login
Content-Type: application/json

{
  "login": "admin",
  "senha": "admin"
}

# Resposta:
{
  "jwt": "eyJhbGc...",
  "expirationMinutes": 120,
  "expiresAt": "2026-02-16T19:30:00Z"
}
```

**✅ Feito! Token obtido!**

---

### Passo 2: Usar Token nos Endpoints

```bash
# 2️⃣ GET Listar (Reutiliza token do Passo 1)
GET http://localhost:8080/ficha-tecnica/unidades-medida
Authorization: Bearer eyJhbGc...

# Resposta:
[
  {"id": 1, "descricao": "Quilograma"},
  {"id": 2, "descricao": "Litro"}
]

# 3️⃣ GET por ID (Reutiliza MESMO token)
GET http://localhost:8080/ficha-tecnica/unidades-medida/1
Authorization: Bearer eyJhbGc...

# Resposta:
{"id": 1, "descricao": "Quilograma"}

# 4️⃣ POST Criar (Reutiliza MESMO token)
POST http://localhost:8080/ficha-tecnica/unidades-medida
Authorization: Bearer eyJhbGc...
Content-Type: application/json

{"descricao": "Mililitro"}

# Resposta:
{"id": 3, "descricao": "Mililitro"}

# 5️⃣ PUT Atualizar (Reutiliza MESMO token)
PUT http://localhost:8080/ficha-tecnica/unidades-medida/1
Authorization: Bearer eyJhbGc...
Content-Type: application/json

{"descricao": "Kilo"}

# Resposta:
{"id": 1, "descricao": "Kilo"}

# 6️⃣ DELETE (Reutiliza MESMO token)
DELETE http://localhost:8080/ficha-tecnica/unidades-medida/3
Authorization: Bearer eyJhbGc...

# Resposta:
204 No Content
```

**✅ Token reutilizado em TODAS as requisições!**
**✅ SEM necessidade de refazer login!**

---

### Passo 3: Token Expira

```bash
# Após 120 minutos (ou tempo configurado)
GET http://localhost:8080/ficha-tecnica/unidades-medida
Authorization: Bearer eyJhbGc...

# Resposta:
401 Unauthorized
{
  "error": "Token expirado",
  "message": "Seu token de autenticação expirou. Por favor, faça login novamente."
}
```

**✅ Cliente deve:**
- Limpar localStorage
- Redirecionar para login
- Fazer login novamente

---

## 📊 Diagrama de Fluxo

```
CLIENTE                    SERVIDOR
  │                           │
  │─ POST /auth/login ────────>│
  │                           │
  │<── { jwt, ... } ──────────│
  │                           │
  │ (Armazena jwt)            │
  │                           │
  ├─ GET /unidades-medida ──->│
  │  Headers: Authorization    │
  │                           │ SecurityFilter
  │                           ├─ Extrai token
  │                           ├─ Valida expiração
  │                           ├─ ✅ Token válido
  │                           │
  │<── 200 OK [dados] ────────│
  │                           │
  │ (Reutiliza MESMO jwt)     │
  │                           │
  ├─ POST /unidades-medida ──>│
  │  Headers: Authorization    │
  │                           │ SecurityFilter
  │                           ├─ Extrai token
  │                           ├─ Valida expiração
  │                           ├─ ✅ Token ainda válido
  │                           │
  │<── 200 OK [nova] ─────────│
  │                           │
  │ (APÓS 120 MINUTOS)        │
  │                           │
  │─ GET /unidades-medida ──->│
  │  Headers: Authorization    │
  │                           │ SecurityFilter
  │                           ├─ Extrai token
  │                           ├─ Valida expiração
  │                           ├─ ❌ Token EXPIRADO!
  │                           │
  │<── 401 Unauthorized ──────│
  │    { "error": "Token..." }│
  │                           │
  │ (Cliente faz login novamente)
  │                           │
  └───────────────────────────┘
```

---

## 🔐 Como Funciona a Segurança

### SecurityConfigurations.java

```java
// Todos os endpoints EXCETO login e imagens 
// precisam de autenticação
.authorizeHttpRequests(ar->ar
    .requestMatchers("/images/**").permitAll()
    .requestMatchers(HttpMethod.POST,"/auth/login").permitAll()
    .anyRequest().authenticated())  // ← MedidasController entra aqui!
```

### SecurityFilter.java

```java
// Intercepta TODA requisição
protected void doFilterInternal(...) {
    // 1. Extrai token do header "Authorization: Bearer ..."
    var tokenJWT = recuperarToken(request);
    
    if(tokenJWT != null) {
        // 2. Valida se token não expirou
        if(!tokenService.validarTokenExpirado(tokenJWT)){
            // ❌ Token expirado
            return 401 "Token expirado";
        }
        
        // 3. Token válido - extrai dados
        var subject = tokenService.getSubject(tokenJWT);
        var role = tokenService.getRole(tokenJWT);
        
        // 4. Autentica no Spring Security
        var authentication = new UsernamePasswordAuthenticationToken(...);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    
    // 5. Processa requisição
    filterChain.doFilter(request, response);
}
```

---

## 📱 IMPLEMENTAÇÃO NO FRONTEND

### JavaScript/React

```javascript
// 1. Login (UMA VEZ)
const login = async () => {
  const response = await fetch('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ login, senha })
  });
  const data = await response.json();
  localStorage.setItem('jwtToken', data.jwt);
};

// 2. Usar token em requisições (AUTOMATICAMENTE)
const fetchWithToken = async (url, options = {}) => {
  const token = localStorage.getItem('jwtToken');
  const headers = {
    'Authorization': `Bearer ${token}`,
    ...options.headers
  };
  
  const response = await fetch(url, {
    ...options,
    headers
  });
  
  // Se token expirou, fazer login novamente
  if (response.status === 401) {
    localStorage.removeItem('jwtToken');
    window.location.href = '/login';
  }
  
  return response;
};

// 3. Usar em requisições
const unidades = await fetchWithToken('/ficha-tecnica/unidades-medida');
const data = await unidades.json();
// Token foi adicionado automaticamente!
```

---

## 🧪 TESTE RÁPIDO

### PowerShell

```powershell
# 1. Login
$response = Invoke-WebRequest -Uri "http://localhost:8080/auth/login" `
  -Method POST `
  -Headers @{"Content-Type"="application/json"} `
  -Body '{"login":"admin","senha":"admin"}'

$TOKEN = ($response.Content | ConvertFrom-Json).jwt

# 2. Usar token (1ª requisição)
Invoke-WebRequest -Uri "http://localhost:8080/ficha-tecnica/unidades-medida" `
  -Method GET `
  -Headers @{"Authorization"="Bearer $TOKEN"}

# 3. Usar token (2ª requisição - REUTILIZA!)
Invoke-WebRequest -Uri "http://localhost:8080/ficha-tecnica/unidades-medida/1" `
  -Method GET `
  -Headers @{"Authorization"="Bearer $TOKEN"}

# 4. Usar token (3ª requisição - REUTILIZA!)
Invoke-WebRequest -Uri "http://localhost:8080/ficha-tecnica/unidades-medida" `
  -Method POST `
  -Headers @{
    "Authorization"="Bearer $TOKEN"
    "Content-Type"="application/json"
  } `
  -Body '{"descricao":"Mililitro"}'

# ✅ MESMO TOKEN REUTILIZADO 3 VEZES!
```

---

## 📋 Resumo da Solução

### ✅ Questão 01: Login Uma Vez

```
✓ Cliente faz POST /auth/login
✓ Servidor retorna JWT + expirationMinutes + expiresAt
✓ Cliente armazena em localStorage
✓ FEITO! Token pronto para usar
```

### ✅ Questão 02: Usar Token Sem Refazer Login

```
✓ Client adiciona token no header "Authorization: Bearer ..."
✓ SecurityFilter intercepta e valida
✓ Se válido: Processa requisição normalmente
✓ Se expirado: Retorna 401 com mensagem clara
✓ Token reutilizado em TODAS as requisições
✓ SEM necessidade de refazer login enquanto válido
```

---

## 🎯 Próximas Ações

### No Backend (Já Está Pronto ✅)
- [x] Endpoints protegidos por JWT
- [x] Token com expiração configurável
- [x] Validação automática em cada requisição
- [x] Mensagens de erro apropriadas

### No Frontend (Você Precisa Fazer)
- [ ] Página de login
- [ ] Armazenar token em localStorage
- [ ] Adicionar token em todas as requisições
- [ ] Tratar erro 401 (token expirado)
- [ ] Redirecionar para login se necessário
- [ ] Proteção de rotas (verificar autenticação)

---

## 📚 Documentação Criada

Para entender melhor, consulte:

1. **COMO_USAR_JWT_NOS_ENDPOINTS.md**
   - Fluxo completo passo-a-passo
   - Exemplos práticos
   - Implementação em React
   - Diagrama de arquitetura

2. **TESTES_PRATICOS_MEDIDAS_CONTROLLER.md**
   - Testes com PowerShell
   - Postman Collection
   - Script completo de teste
   - Casos de erro

3. **RESUMO_FINAL.md**
   - Quick start
   - Como configurar
   - Teste rápido

---

## ✨ Resumo Final

```
┌─────────────────────────────────────────┐
│ ✅ SOLUÇÃO COMPLETA                    │
├─────────────────────────────────────────┤
│                                         │
│ ✓ Login uma vez                        │
│ ✓ Obtém token com expiração            │
│ ✓ Reutiliza token em endpoints         │
│ ✓ Valida automaticamente               │
│ ✓ Retorna erro quando expira           │
│ ✓ Sem necessidade de refazer login     │
│                                         │
│ STATUS: PRONTO PARA PRODUÇÃO! 🚀       │
│                                         │
└─────────────────────────────────────────┘
```

---

**Data**: 16 de Fevereiro de 2026
**Status**: ✅ IMPLEMENTADO E DOCUMENTADO
**Suporte**: Documentação completa incluída

Aproveite! 🎯

