# 🛒 Backend de Caixa de Supermercado — Produtos e Vendas

Este sistema backend é focado em dois domínios principais:

1. **Produtos** — controle de estoque (nome, código de barras, preço)
2. **Vendas** — registro de compras com total indexado

---

## 📦 DTOs (Data Transfer Objects)

### ProdutoDTO

    {
      "id": "123",
      "codigo_barras": "7891234567890",
      "nome": "Refrigerante Cola 2L",
      "preco": 6.50
    }

### VendaProdutoDTO (item vendido em uma venda)

    {
      "produto_id": "123",
      "quantidade": 2,
      "preco_unitario": 6.50
    }

### VendaDTO (detalhes de uma venda completa)

    {
      "id": "venda_001",
      "data": "2025-05-13T15:34:00Z",
      "forma_pagamento": "dinheiro",
      "itens": [
        {
          "produto_id": "123",
          "quantidade": 2,
          "preco_unitario": 6.50
        },
        {
          "produto_id": "456",
          "quantidade": 1,
          "preco_unitario": 12.00
        }
      ],
      "total": 25.00
    }

---

## 🔁 Fluxo geral do frontend (client)

1. Usuário no caixa escaneia o código de barras
2. O client envia o código para o backend e recebe nome e preço
3. O client exibe os itens na tela e permite ajuste de quantidades
4. Ao finalizar a venda:
    - O client envia os produtos (id, quantidade, preço) e forma de pagamento
    - O backend calcula e salva o total

---

## 🛒 Comportamento do client

### Durante a venda:

- Escaneia o código de barras → GET /produtos/codigo/{codigo_barras}
- Mostra os produtos em uma lista local com quantidades
- Mantém o total parcial atualizado no frontend

### Ao finalizar a venda:

- Envia para o backend:
    - Lista de produtos (produto_id, quantidade, preco_unitario)
    - Forma de pagamento
- O backend registra a venda e calcula/salva o total

---

## 📡 Endpoints

### 🔍 Buscar produto por código de barras

GET /produtos/codigo/{codigo_barras}

**Resposta (200 OK):**

    {
      "id": "123",
      "nome": "Sabão em pó 1kg",
      "preco": 7.80,
      "codigo_barras": "7891234567890"
    }

**Resposta (404 Not Found):**

    {
      "erro": "Produto não encontrado"
    }

---

### ➕ Criar nova venda

POST /vendas

**Request:**

    {
      "forma_pagamento": "cartao",
      "itens": [
        {
          "produto_id": "123",
          "quantidade": 2,
          "preco_unitario": 6.50
        },
        {
          "produto_id": "456",
          "quantidade": 1,
          "preco_unitario": 12.00
        }
      ]
    }

**Resposta (201 Created):**

    {
      "id": "venda_001",
      "data": "2025-05-13T15:34:00Z",
      "forma_pagamento": "cartao",
      "itens": [ ... ],
      "total": 25.00
    }

**Resposta (400 Bad Request):**

    {
      "erro": "Dados inválidos"
    }

---

### 📄 Listar vendas

GET /vendas

**Resposta (200 OK):**

    [
      {
        "id": "venda_001",
        "data": "2025-05-13T15:34:00Z",
        "forma_pagamento": "cartao",
        "itens": [ ... ],
        "total": 25.00
      },
      {
        "id": "venda_002",
        "data": "2025-05-13T16:00:00Z",
        "forma_pagamento": "dinheiro",
        "itens": [ ... ],
        "total": 18.50
      }
    ]

**Resposta (404 Not Found):**

    {
      "erro": "Nenhuma venda encontrada"
    }

---

### 🧾 Ver detalhes de uma venda

GET /vendas/{id}

**Resposta (200 OK):**

    {
      "id": "venda_001",
      "data": "2025-05-13T15:34:00Z",
      "forma_pagamento": "cartao",
      "itens": [
        {
          "produto_id": "123",
          "quantidade": 2,
          "preco_unitario": 6.50
        },
        {
          "produto_id": "456",
          "quantidade": 1,
          "preco_unitario": 12.00
        }
      ],
      "total": 25.00
    }

**Resposta (404 Not Found):**

    {
      "erro": "Venda não encontrada"
    }

---

## ✅ Resumo — O que o client faz?

| Ação              | O que o frontend faz |
|-------------------|----------------------|
| Adicionar produto | Envia código de barras → recebe nome/preço/id |
| Mostrar carrinho  | Mantém localmente a lista de produtos com quantidades e preços |
| Finalizar venda   | Envia lista de produto_id, quantidade, preco_unitario, e forma de pagamento |
| Total da compra   | O backend calcula e salva o total |
