# 🛒 Backend de Caixa de Supermercado — Produtos e Vendas

Este sistema backend é focado em dois domínios principais:

1. **Produtos** — controle de estoque (nome, código de barras, preço)
2. **Vendas** — registro de compras com total indexado

Após estabilização, será incluídas instruções de build/run para este projeto,
as quais usarão `Docker`.

Também não temos conexão com nenhum serviço real de pagamentos, por enquanto,
o pagamento é simplesmente simulado e sempre válido. Após estabilização da API,
iremos integrar com algum serviço de pagamento compatível com Crédito, Débito
e/ou Pix.

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
    Body: ProductDTO (ver definição abaixo)

### ⚠️ Erros Possíveis

- 400 Bad Request – Se `barcode` estiver ausente.
- 404 Not Found – Produto não encontrado ou sem estoque.

---

## ➕ POST /products

Cria um novo produto no sistema (e registra seu estoque inicial).

### 📥 Corpo da Requisição

    Content-Type: application/json
    Body: ProductDTO (sem campo "id")

    {
        "barcode": "1234567890123",
        "name": "Produto Teste",
        "price": 10,
        "quantity": 100
    }

### ✅ Resposta Esperada

    Status: 201 Created
    Body: ID do produto criado (número inteiro)

### ⚠️ Erros Possíveis

- 422 Unprocessable Entity – Produto já cadastrado ou erro ao inserir no banco.

---

## 💰 POST /sales

Registra uma nova venda com a lista de itens comprados e a forma de pagamento.

### 📥 Corpo da Requisição

    Content-Type: application/json
    Body: SaleRequestDTO

    {
        "paymentType": "Pix",
        "date": "2025-05-14T15:00:00Z",  // opcional: pode ser omitido
        "items": [
            {
                "barcode": "1234567890123",
                "quantity": 2
            }
        ]
    }

### ✅ Resposta Esperada

    Status: 201 Created
    Body: ID da venda (número inteiro)

### ⚠️ Erros Possíveis

- 422 Unprocessable Entity – Produto não encontrado, estoque insuficiente ou falha no pagamento.

---

## 📄 GET /sales

Lista todas as vendas já registradas no sistema, incluindo detalhes dos itens vendidos.

### ✅ Resposta Esperada

    Status: 200 OK
    Body: Lista de SaleDetailDTO

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

- 500 Internal Server Error – Falha ao buscar vendas no banco.

---

## 🧱 Modelos de Dados (DTOs)

### 📦 ProductDTO

Representa um produto no sistema, tanto na entrada (criação) quanto na resposta da API.

#### Campos

- `id` (opcional na criação): identificador interno do produto.
- `barcode`: código de barras único do produto.
- `name`: nome do produto.
- `price`: preço unitário.
- `quantity`: quantidade atual em estoque (na criação) ou disponível (em consulta).

#### Exemplo

    {
        "id": 1,
        "barcode": "1234567890123",
        "name": "Produto Teste",
        "price": 10,
        "quantity": 100
    }

---

### 🛒 ProductSaleDTO

Objeto que representa um item sendo comprado durante uma venda.

#### Campos

- `barcode`: código de barras do produto a ser vendido.
- `quantity`: quantidade desejada.

#### Exemplo

    {
        "barcode": "1234567890123",
        "quantity": 2
    }

---

### 🧾 SaleRequestDTO

Objeto enviado pelo client para registrar uma venda.

#### Campos

- `paymentType`: forma de pagamento usada.
- `date` (opcional): data/hora da venda (UTC). Pode ser omitida e o backend registra o horário atual.
- `items`: lista de `ProductSaleDTO`, representando os produtos vendidos.

#### Exemplo

    {
        "paymentType": "Pix",
        "date": "2025-05-14T15:00:00Z",
        "items": [
            {
                "barcode": "1234567890123",
                "quantity": 2
            }
        ]
    }

---

### 📄 SaleDetailDTO

Objeto retornado pela API representando os detalhes completos de uma venda.

#### Campos

- `id`: identificador da venda.
- `date`: data e hora da venda.
- `paymentType`: forma de pagamento usada.
- `items`: lista de `ProductSoldDTO`.

#### Exemplo

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

### 🧾 ProductSoldDTO

Objeto que representa um item vendido, com informações da venda.

#### Campos

- `productId`: identificador do produto vendido.
- `quantitySold`: quantidade vendida.
- `priceAtMoment`: preço do produto no momento da venda.

#### Exemplo

    {
        "productId": 1,
        "quantitySold": 2,
        "priceAtMoment": 10
    }

---

## 💳 Formas de Pagamento Válidas

Campo `paymentType` aceita os seguintes valores:

- `"Cash"`
- `"Credit"`
- `"Debit"`
- `"Pix"`