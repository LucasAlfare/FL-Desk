import React, { useState } from 'react';

function BarcodeInputSaleComponent({ apiBaseUrl, onSaleCompleted }) {
  const [barcode, setBarcode] = useState('');
  const [quantity, setQuantity] = useState(1);
  const [items, setItems] = useState([]);
  const [paymentType, setPaymentType] = useState('Cash');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Calculate total
  const total = items.reduce((sum, item) => sum + item.price * item.requestedQuantity, 0);

  // Handle adding a product by barcode
  const addItem = async () => {
    if (!barcode) return;
    setLoading(true);
    setError(null);
    try {
      const response = await fetch(
        `${apiBaseUrl}/products/barcode/${encodeURIComponent(barcode)}`
      );
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }
      const product = await response.json();
      // add with requested quantity
      setItems(prev => [
        ...prev,
        { ...product, requestedQuantity: quantity }
      ]);
      setBarcode('');
      setQuantity(1);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  // Handle finalize sale
  const finalizeSale = async () => {
    if (items.length === 0) return;
    const saleRequest = {
      paymentType,
      items: items.map(item => ({
        barcode: item.barcode,
        quantity: item.requestedQuantity
      }))
    };
    setLoading(true);
    setError(null);
    try {
      const resp = await fetch(`${apiBaseUrl}/sales`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(saleRequest)
      });
      if (!resp.ok) throw new Error(`HTTP ${resp.status}`);
      const result = await resp.json();
      // callback for parent
      onSaleCompleted(result);
      // reset
      setItems([]);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <div style={{ marginBottom: '1rem' }}>
        <input
          type="text"
          placeholder="Código de barras"
          value={barcode}
          onChange={e => setBarcode(e.target.value)}
          disabled={loading}
          style={{ marginRight: '0.5rem' }}
        />
        <input
          type="number"
          min="1"
          value={quantity}
          onChange={e => setQuantity(parseInt(e.target.value, 10) || 1)}
          disabled={loading}
          style={{ width: '4rem', marginRight: '0.5rem' }}
        />
        <button onClick={addItem} disabled={loading}>
          Add
        </button>
      </div>

      {error && <div style={{ color: 'red' }}>{error}</div>}

      <ul>
        {items.map((item, idx) => (
          <li key={`${item.barcode}-${idx}`}>
            {item.barcode} | {item.name} | R$ {(item.price / 100).toFixed(2)} ({item.requestedQuantity})
          </li>
        ))}
      </ul>

      <div style={{ marginTop: '1rem' }}>
        <strong>Total: R$ {(total / 100).toFixed(2)}</strong>
      </div>

      <div style={{ marginTop: '1rem' }}>
        <label>
          Tipo de pagamento:{' '}
          <select
            value={paymentType}
            onChange={e => setPaymentType(e.target.value)}
            disabled={loading}
          >
            <option value="Cash">Dinheiro</option>
            <option value="Credit">Cartão de crédito</option>
            <option value="Debit">Cartão débito</option>
            <option value="Pix">PIX</option>
          </select>
        </label>
      </div>

      <button
        onClick={finalizeSale}
        disabled={loading || items.length === 0}
        style={{ marginTop: '1rem' }}
      >
        Finalizar
      </button>

      {loading && <div>Carregando...</div>}
    </div>
  );
}

function App() {
  const handleSaleCompleted = (result) => {
    console.log('Venda finalizada com sucesso:', result);
    alert('Venda registrada com sucesso!');
  };

  return (
    <div>
      <h1>Registrar Venda</h1>
      <BarcodeInputSaleComponent
        apiBaseUrl="http://localhost:3000"
        onSaleCompleted={handleSaleCompleted}
      />
    </div>
  )
}

export default App
