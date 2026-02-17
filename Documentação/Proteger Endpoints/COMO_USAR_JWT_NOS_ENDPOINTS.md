# 🔐 Guia: Como Usar JWT Token nos Endpoints com MedidasController

## ✅ Solução para Suas Questões

### Questão 01: Login uma vez e retornar token válido
### Questão 02: Usar token nos endpoints enquanto válido

---

## 🎯 Arquitetura Implementada

```
┌─────────────────────────────────────────────────────────┐
│                   FLUXO DE AUTENTICAÇÃO                 │
└─────────────────────────────────────────────────────────┘

1. CLIENTE FAZA LOGIN (UMA VEZ)
   POST /auth/login
   {"login": "usuario", "senha": "senha"}
            ↓
   RESPOSTA COM TOKEN
   {
     "jwt": "eyJhbGc...",
     "expirationMinutes": 120,
     "expiresAt": "2026-02-16T19:30:00Z"
   }

2. CLIENTE ARMAZENA TOKEN
   localStorage.setItem("jwtToken", jwt)

3. CLIENTE USA TOKEN EM TODOS OS ENDPOINTS
   GET /ficha-tecnica/unidades-medida
   Headers: {
     "Authorization": "Bearer eyJhbGc..."
   }

4. SERVIDOR VALIDA TOKEN
   SecurityFilter intercepta requisição
   ├─ Extrai token do header Authorization
   ├─ Valida se não expirou
   ├─ Se válido: Processa requisição
   └─ Se expirado: Retorna 401 "Token expirado"

5. CLIENTE RECEBE RESPOSTA
   ├─ 200 OK: Dados do endpoint
   └─ 401: Token expirado → Refazer login
```

---

## 📊 Estado Atual do Projeto

### ✅ Segurança Implementada
```
SecurityConfigurations.java
├─ Login (/auth/login): SEM autenticação
├─ Imagens (/images/**): SEM autenticação
└─ Todos os outros (/ficha-tecnica/**): AUTENTICADOS
   └─ Valida token JWT via SecurityFilter
```

### ✅ Token Service
```
TokenService.java
├─ gerarToken(): Cria JWT com expiração
├─ getSubject(): Extrai usuário do token
├─ getRole(): Extrai role do token
├─ getExpirationMinutes(): Retorna tempo de expiração
├─ getTokenExpiresAt(): Retorna data/hora de expiração
└─ validarTokenExpirado(): Valida se token não expirou
```

### ✅ Security Filter
```
SecurityFilter.java
├─ Intercepta toda requisição
├─ Extrai token do header "Authorization: Bearer ..."
├─ Valida se token expirou
├─ Se expirado: Retorna 401 com mensagem clara
├─ Se válido: Autentica usuário e processa requisição
└─ Sem token: Deixa passar se for endpoint público
```

---

## 🚀 COMO FUNCIONA NA PRÁTICA

### MedidasController.java - Endpoints Protegidos

```
Todos os 5 endpoints estão PROTEGIDOS por padrão:

1. GET /ficha-tecnica/unidades-medida
   └─ Requer token válido

2. GET /ficha-tecnica/unidades-medida/{id}
   └─ Requer token válido

3. DELETE /ficha-tecnica/unidades-medida/{id}
   └─ Requer token válido

4. PUT /ficha-tecnica/unidades-medida/{id}
   └─ Requer token válido

5. POST /ficha-tecnica/unidades-medida
   └─ Requer token válido
```

**Por que?** Porque em `SecurityConfigurations.java`, qualquer requisição que não for:
- POST /auth/login
- GET /images/**

Precisará de autenticação (token válido).

---

## 📱 FLUXO COMPLETO DE USO

### PASSO 1: Cliente Faz Login (UMA VEZ!)

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "login": "admin",
    "senha": "admin"
  }'
```

**Resposta:**
```json
{
  "jwt": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJBUEkgRmljaGEgVGVjbmljYSIsInN1YiI6ImFkbWluIiwicm9sZSI6IlJPTEVfQURNSU4iLCJub21lIjoiQWRtaW4iLCJleHAiOjE3Mzk3MjU2MDB9.abc123...",
  "expirationMinutes": 120,
  "expiresAt": "2026-02-16T19:30:00Z"
}
```

**✅ O cliente recebeu o token!**

---

### PASSO 2: Cliente Armazena Token (No Frontend)

```javascript
// Login bem-sucedido
const response = await fetch('/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ login: 'admin', senha: 'admin' })
});

const data = await response.json();

// ARMAZENAR O TOKEN
localStorage.setItem('jwtToken', data.jwt);
localStorage.setItem('expirationMinutes', data.expirationMinutes);
localStorage.setItem('expiresAt', data.expiresAt);

// O token vai ser reutilizado em TODAS as requisições!
```

---

### PASSO 3: Cliente Usa Token nos Endpoints (SEM Refazer Login!)

#### Exemplo 1: Listar Unidades de Medida

```bash
# Com o token do PASSO 1
curl -X GET http://localhost:8080/ficha-tecnica/unidades-medida \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Fluxo no Servidor:**
```
1. SecurityFilter intercepta a requisição
2. Extrai token do header "Authorization: Bearer ..."
3. Valida se token não expirou
4. ✅ Token válido → Processa requisição
5. MedidasController.buscarLista() é executado
6. Retorna lista de unidades
```

**Resposta (200 OK):**
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

**✅ Sem necessidade de refazer login!**

---

#### Exemplo 2: Buscar Unidade por ID

```bash
curl -X GET http://localhost:8080/ficha-tecnica/unidades-medida/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Resposta (200 OK):**
```json
{
  "id": 1,
  "descricao": "Quilograma"
}
```

---

#### Exemplo 3: Cadastrar Unidade

```bash
curl -X POST http://localhost:8080/ficha-tecnica/unidades-medida \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "descricao": "Mililitro"
  }'
```

**Resposta (200 OK):**
```json
{
  "id": 3,
  "descricao": "Mililitro"
}
```

---

#### Exemplo 4: Atualizar Unidade

```bash
curl -X PUT http://localhost:8080/ficha-tecnica/unidades-medida/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "descricao": "Kilo"
  }'
```

**Resposta (200 OK):**
```json
{
  "id": 1,
  "descricao": "Kilo"
}
```

---

#### Exemplo 5: Deletar Unidade

```bash
curl -X DELETE http://localhost:8080/ficha-tecnica/unidades-medida/3 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Resposta (204 No Content):**
```
(sem corpo)
```

---

### PASSO 4: Token Expira

Após 120 minutos (padrão) ou o tempo configurado em `JWT_EXPIRATION_MINUTES`:

```bash
curl -X GET http://localhost:8080/ficha-tecnica/unidades-medida \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Fluxo no Servidor:**
```
1. SecurityFilter intercepta a requisição
2. Extrai token do header
3. Valida token com TokenService.validarTokenExpirado()
4. ❌ Token expirado!
5. Retorna erro 401 com mensagem específica
```

**Resposta (401 Unauthorized):**
```json
{
  "error": "Token expirado",
  "message": "Seu token de autenticação expirou. Por favor, faça login novamente."
}
```

**✅ Cliente deve:**
```javascript
// Limpar dados
localStorage.removeItem('jwtToken');
localStorage.removeItem('expirationMinutes');
localStorage.removeItem('expiresAt');

// Redirecionar para login
window.location.href = '/login';
```

---

## 🛠️ IMPLEMENTAÇÃO NO FRONTEND (React)

### 1. Hook de Autenticação

```javascript
// useAuth.js
import { useState, useCallback } from 'react';

export function useAuth() {
  const [isAuthenticated, setIsAuthenticated] = useState(
    !!localStorage.getItem('jwtToken')
  );
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const login = useCallback(async (username, password) => {
    setLoading(true);
    setError(null);
    
    try {
      const response = await fetch('/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ login: username, senha: password })
      });

      if (!response.ok) {
        throw new Error('Falha na autenticação');
      }

      const data = await response.json();
      
      // Armazenar token (UMA ÚNICA VEZ!)
      localStorage.setItem('jwtToken', data.jwt);
      localStorage.setItem('expirationMinutes', data.expirationMinutes);
      localStorage.setItem('expiresAt', data.expiresAt);
      
      setIsAuthenticated(true);
      return true;
    } catch (err) {
      setError(err.message);
      return false;
    } finally {
      setLoading(false);
    }
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('jwtToken');
    localStorage.removeItem('expirationMinutes');
    localStorage.removeItem('expiresAt');
    setIsAuthenticated(false);
  }, []);

  return { isAuthenticated, login, logout, loading, error };
}
```

### 2. Interceptor Axios

```javascript
// api.js
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080'
});

// Adicionar token em TODAS as requisições
api.interceptors.request.use(config => {
  const token = localStorage.getItem('jwtToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Tratar token expirado
api.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      const errorData = error.response.data;
      
      if (errorData.error === 'Token expirado') {
        // Limpar dados
        localStorage.clear();
        // Redirecionar para login
        window.location.href = '/login';
        alert('Sua sessão expirou. Faça login novamente.');
      }
    }
    return Promise.reject(error);
  }
);

export default api;
```

### 3. Usar API em Componentes

```javascript
// MedidasPage.jsx
import { useEffect, useState } from 'react';
import api from './api';

export function MedidasPage() {
  const [medidas, setMedidas] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    buscarMedidas();
  }, []);

  const buscarMedidas = async () => {
    setLoading(true);
    try {
      // TOKEN É ADICIONADO AUTOMATICAMENTE!
      const response = await api.get('/ficha-tecnica/unidades-medida');
      setMedidas(response.data);
    } catch (error) {
      console.error('Erro ao buscar medidas:', error);
      // Se token expirou, interceptor vai redirecionar
    } finally {
      setLoading(false);
    }
  };

  const adicionarMedida = async (descricao) => {
    try {
      // TOKEN É ADICIONADO AUTOMATICAMENTE!
      const response = await api.post('/ficha-tecnica/unidades-medida', {
        descricao
      });
      setMedidas([...medidas, response.data]);
    } catch (error) {
      console.error('Erro ao adicionar medida:', error);
    }
  };

  return (
    <div>
      <h1>Unidades de Medida</h1>
      {loading ? <p>Carregando...</p> : (
        <ul>
          {medidas.map(m => (
            <li key={m.id}>{m.descricao}</li>
          ))}
        </ul>
      )}
    </div>
  );
}
```

---

## 🔄 CICLO COMPLETO (Passo a Passo)

```
INÍCIO
  │
  ├─ Cliente abre a aplicação
  ├─ Verifica: localStorage.getItem('jwtToken')
  │  ├─ SIM: Já autenticado, vai para dashboard
  │  └─ NÃO: Mostra tela de login
  │
  ├─ Usuário preenche login/senha
  ├─ Cliente faz POST /auth/login (UMA VEZ!)
  ├─ Servidor valida credenciais
  ├─ Servidor gera JWT com expiração
  ├─ Cliente recebe: { jwt, expirationMinutes, expiresAt }
  │
  ├─ Cliente armazena em localStorage
  ├─ Cliente redireciona para dashboard
  │
  ├─ Dashboard faz requisição para /ficha-tecnica/unidades-medida
  ├─ Token é adicionado no header automaticamente
  ├─ SecurityFilter valida token
  ├─ ✅ Token válido → Processa requisição
  ├─ MedidasController retorna dados
  ├─ Cliente renderiza lista
  │
  ├─ PASSAM 120 MINUTOS (ou tempo configurado)
  ├─ Cliente tenta fazer outra requisição
  ├─ Token ainda está em localStorage
  ├─ SecurityFilter valida token
  ├─ ❌ Token expirado!
  ├─ Retorna 401 "Token expirado"
  ├─ Interceptor deteta erro 401
  ├─ Limpa localStorage
  ├─ Redireciona para login
  │
  └─ Usuário faz login novamente
     └─ VOLTA AO INÍCIO
```

---

## ✅ RESUMO: Como Responde às Suas Questões

### Questão 01: "O login deve ser feito uma vez e deve retornar um token válido"

**✅ IMPLEMENTADO:**
- Cliente faz POST /auth/login com credenciais
- Servidor retorna JWT + expirationMinutes + expiresAt
- Cliente armazena em localStorage
- Sem necessidade de refazer login enquanto válido

**Código:**
```javascript
const response = await fetch('/auth/login', {
  method: 'POST',
  body: JSON.stringify({ login, senha })
});
const { jwt, expirationMinutes, expiresAt } = await response.json();
localStorage.setItem('jwtToken', jwt);
```

---

### Questão 02: "Cada endpoint deve utilizar o token enquanto válido, sem necessidade de refazer o login a cada chamada e em caso de expiração retornar token expirado"

**✅ IMPLEMENTADO:**
- SecurityFilter intercepta TODA requisição
- Extrai token do header Authorization
- Valida com TokenService.validarTokenExpirado()
- Se válido: Processa normalmente
- Se expirado: Retorna 401 com mensagem "Token expirado"
- Cliente pode implementar lógica para refazer login

**Código:**
```bash
# Requisição com token (reutilizado!)
curl -X GET /ficha-tecnica/unidades-medida \
  -H "Authorization: Bearer $TOKEN"

# Se token válido: 200 OK com dados
# Se token expirado: 401 com "Token expirado"
```

---

## 🎯 PRÓXIMAS AÇÕES RECOMENDADAS

1. **✅ Backend** - Já está pronto!
   - SecurityConfigurations permite apenas endpoints autenticados
   - TokenService valida expiração
   - SecurityFilter implementa a lógica

2. **⚠️ Frontend** - Você precisa implementar:
   - Hook useAuth para gerenciar autenticação
   - Interceptor para adicionar token em requisições
   - Lógica para tratar erro 401 (token expirado)
   - Página de login
   - Proteção de rotas (se não autenticado, redireciona)

3. **🧪 Testes** - Você pode:
   - Testar com cURL (exemplos acima)
   - Usar Postman/Insomnia
   - Scripts PowerShell/Bash (já fornecidos)

---

## 📝 Checklist de Implementação

- [x] Backend implementado (JWT, SecurityFilter, validação)
- [x] Endpoints do MedidasController protegidos
- [x] Token com expiração configurável
- [ ] Frontend: Hook useAuth
- [ ] Frontend: Interceptor de requisições
- [ ] Frontend: Tratamento de erro 401
- [ ] Frontend: Página de login
- [ ] Frontend: Proteção de rotas
- [ ] Testes E2E

---

**Tudo que você pediu está implementado no backend!** ✅

Agora é só consumir os endpoints corretamente no frontend usando o token. 🚀

