# 🛒 Backend de Caixa de Supermercado — Produtos e Vendas

Este sistema backend é focado em dois domínios principais:

1. **Produtos** — controle de estoque (nome, código de barras, preço)
2. **Vendas** — registro de compras com total indexado

## 🌐 Base URL

    http://localhost:3000

---

## ✅ GET /health

Verifica se a API está operando corretamente.

### ✅ Resposta Esperada

    Status: 200 OK
    Body: "Hello from KTOR!"

---

## 📦 GET /products/barcode/{barcode}

Busca informações de um produto pelo código de barras.

### 🔧 Parâmetros

- barcode (path param): código de barras do produto (string).

### 🔄 Exemplo de Requisição

    GET /products/barcode/1234567890123

### ✅ Resposta Esperada

    Status: 200 OK
    Body:
        {
            "id": 1,
            "barcode": "1234567890123",
            "name": "Produto Teste",
            "price": 10,
            "quantity": 100
        }

### ⚠️ Erros Possíveis

- 400 Bad Request – Se `barcode` estiver ausente.
- 404 Not Found – Produto não encontrado ou sem estoque.

---

## ➕ POST /products

Cria um novo produto no sistema.

### 📥 Corpo da Requisição

    Content-Type: application/json

    {
        "barcode": "1234567890123",
        "name": "Produto Teste",
        "price": 10,
        "quantity": 100
    }

### ✅ Resposta Esperada

    Status: 201 Created
    Body: ID do produto criado (número)

### ⚠️ Erros Possíveis

- 422 Unprocessable Entity – Produto já cadastrado ou erro ao inserir no banco.

---

## 💰 POST /sales

Registra uma venda com itens e tipo de pagamento.

### 📥 Corpo da Requisição

    Content-Type: application/json

    {
        "paymentType": "Pix",
        "date": "2025-05-14T15:00:00Z",  // opcional, pode ser omitido se a data for gerada no backend
        "items": [
            {
                "barcode": "1234567890123",
                "quantity": 2
            }
        ]
    }

### ✅ Resposta Esperada

    Status: 201 Created
    Body: ID da venda (número)

### ⚠️ Erros Possíveis

- 422 Unprocessable Entity – Produto não encontrado, estoque insuficiente ou falha no pagamento.

---

## 📄 GET /sales

Lista todas as vendas realizadas.

### ✅ Resposta Esperada

    Status: 200 OK
    Body:
        [
            {
                "id": 1,
                "date": "2025-05-14T15:00:00Z",
                "paymentType": "Pix",
                "items": [
                    {
                        "productId": 1,
                        "quantitySold": 2,
                        "priceAtMoment": 10
                    }
                ]
            }
        ]

### ⚠️ Erros Possíveis

- 500 Internal Server Error – Falha ao buscar vendas do banco.

---

## 📦 Modelos de Dados

### ProductDTO

    {
        "barcode": "1234567890123",
        "name": "Produto Teste",
        "price": 10,
        "quantity": 100
    }

### ProductSaleDTO

    {
        "barcode": "1234567890123",
        "quantity": 2
    }

### SaleRequestDTO

    {
        "paymentType": "Pix",
        "date": "2025-05-14T15:00:00Z",
        "items": [ ...ProductSaleDTO[] ]
    }

### SaleDetailDTO

    {
        "id": 1,
        "date": "2025-05-14T15:00:00Z",
        "paymentType": "Pix",
        "items": [
            {
                "productId": 1,
                "quantitySold": 2,
                "priceAtMoment": 10
            }
        ]
    }

---

## 💳 Tipos de Pagamento

Campo `paymentType` aceita os seguintes valores:

- "Cash"
- "Credit"
- "Debit"
- "Pix"