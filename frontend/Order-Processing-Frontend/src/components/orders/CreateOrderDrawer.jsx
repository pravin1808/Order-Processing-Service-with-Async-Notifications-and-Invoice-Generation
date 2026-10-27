import React, { useState, useEffect } from 'react';
import Drawer from '../common/Drawer';
import { useAuth } from '../../context/AuthContext';
import { createOrder } from '../../api/orders';
import { formatCurrency } from './OrderItemsTable';
import { extractApiError } from '../../api/errorUtils';

export const CreateOrderDrawer = ({ isOpen, onClose, onOrderCreated }) => {
  const { user } = useAuth();

  const [customer, setCustomer] = useState({
    customerName: '',
    customerEmail: '',
    customerPhone: '',
    deliveryAddress: '',
  });

  const [items, setItems] = useState([
    { productName: 'MacBook Pro 16" M3', quantity: 1, unitPrice: '199990' },
  ]);

  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (user && isOpen) {
      setCustomer((prev) => ({
        customerName: user.name || '',
        customerEmail: user.email || '',
        customerPhone: prev.customerPhone || user.phone || '',
        deliveryAddress: prev.deliveryAddress || user.address || '',
      }));
      setError('');
      setFieldErrors({});
    }
  }, [user, isOpen]);

  // Live total calculations
  const calculateTotals = () => {
    let subtotal = 0;
    items.forEach((item) => {
      const q = parseInt(item.quantity, 10) || 0;
      const p = parseFloat(item.unitPrice) || 0;
      subtotal += q * p;
    });
    const tax = subtotal * 0.18;
    const total = subtotal + tax;
    return { subtotal, tax, total };
  };

  const { subtotal, tax, total } = calculateTotals();

  const handleCustomerChange = (e) => {
    const { name, value } = e.target;
    setCustomer((prev) => ({ ...prev, [name]: value }));
    if (fieldErrors[name] || fieldErrors.customerAddress) {
      setFieldErrors((prev) => ({ ...prev, [name]: '', customerAddress: '' }));
    }
  };

  const handleItemChange = (index, field, value) => {
    setItems((prev) => {
      const updated = [...prev];
      updated[index] = { ...updated[index], [field]: value };
      return updated;
    });
    const key1 = `items[${index}].${field}`;
    const key2 = `items.${index}.${field}`;
    if (fieldErrors[key1] || fieldErrors[key2]) {
      setFieldErrors((prev) => {
        const next = { ...prev };
        delete next[key1];
        delete next[key2];
        return next;
      });
    }
  };

  const handleAddItem = () => {
    setItems((prev) => [...prev, { productName: '', quantity: 1, unitPrice: '' }]);
  };

  const handleRemoveItem = (index) => {
    if (items.length <= 1) return;
    setItems((prev) => prev.filter((_, i) => i !== index));
  };

  const handleAddSamplePreset = () => {
    setItems([
      { productName: 'Sony WH-1000XM5 Headphones', quantity: 1, unitPrice: '26990' },
      { productName: 'Logitech MX Master 3S Mouse', quantity: 1, unitPrice: '7995' },
    ]);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!customer.customerName.trim() || !customer.customerEmail.trim()) {
      setError('Customer name and email are required.');
      return;
    }
    if (!customer.deliveryAddress.trim()) {
      setError('Delivery address is required.');
      return;
    }

    for (let i = 0; i < items.length; i++) {
      const item = items[i];
      if (!item.productName.trim()) {
        setError(`Please enter a product name for item #${i + 1}`);
        return;
      }
      const qty = parseInt(item.quantity, 10);
      const price = parseFloat(item.unitPrice);
      if (isNaN(qty) || qty < 1) {
        setError(`Item #${i + 1} must have a quantity of at least 1`);
        return;
      }
      if (isNaN(price) || price <= 0) {
        setError(`Item #${i + 1} must have a valid price greater than ₹0`);
        return;
      }
    }

    try {
      setSubmitting(true);
      setError('');
      setFieldErrors({});

      const orderPayload = {
        customerName: customer.customerName.trim(),
        customerEmail: customer.customerEmail.trim(),
        customerPhone: customer.customerPhone.trim() || null,
        customerAddress: customer.deliveryAddress.trim(),
        items: items.map((it) => ({
          productName: it.productName.trim(),
          quantity: parseInt(it.quantity, 10),
          unitPrice: parseFloat(it.unitPrice),
        })),
      };

      const newOrder = await createOrder(orderPayload);
      onClose();
      if (onOrderCreated) {
        onOrderCreated(newOrder);
      }
    } catch (err) {
      const parsed = await extractApiError(err, 'Failed to create order. Please verify all details.');
      setError(parsed.message);
      setFieldErrors(parsed.fieldErrors || {});
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Drawer
      isOpen={isOpen}
      onClose={onClose}
      title="Create New Order"
      footer={
        <>
          <button type="button" className="btn btn-ghost" onClick={onClose} disabled={submitting}>
            Cancel
          </button>
          <button type="button" className="btn btn-primary" onClick={handleSubmit} disabled={submitting}>
            {submitting ? 'Placing Order...' : 'Place Order →'}
          </button>
        </>
      }
    >
      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
        {error && <div className="alert alert-error" style={{ whiteSpace: 'pre-line' }}>{error}</div>}

        {/* Customer Information */}
        <h4 style={{ borderBottom: '1px solid var(--border)', paddingBottom: 'var(--space-1)' }}>
          Customer Information
        </h4>

        <div className="form-group">
          <label className="form-label" htmlFor="customerName">Full Name *</label>
          <input
            id="customerName"
            name="customerName"
            type="text"
            className={`form-input ${fieldErrors.customerName ? 'error' : ''}`}
            value={customer.customerName}
            onChange={handleCustomerChange}
            placeholder="Recipient's Name"
            required
          />
          {fieldErrors.customerName && <span className="form-error">{fieldErrors.customerName}</span>}
        </div>

        <div className="form-group">
          <label className="form-label" htmlFor="customerEmail">Email Address *</label>
          <input
            id="customerEmail"
            name="customerEmail"
            type="email"
            className={`form-input ${fieldErrors.customerEmail ? 'error' : ''}`}
            value={customer.customerEmail}
            onChange={handleCustomerChange}
            placeholder="name@example.com"
            required
          />
          {fieldErrors.customerEmail && <span className="form-error">{fieldErrors.customerEmail}</span>}
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 'var(--space-3)' }}>
          <div className="form-group">
            <label className="form-label" htmlFor="customerPhone">Phone</label>
            <input
              id="customerPhone"
              name="customerPhone"
              type="tel"
              className={`form-input ${fieldErrors.customerPhone ? 'error' : ''}`}
              value={customer.customerPhone}
              onChange={handleCustomerChange}
              placeholder="+91 9876543210"
            />
            {fieldErrors.customerPhone && <span className="form-error">{fieldErrors.customerPhone}</span>}
          </div>
          <div className="form-group">
            <label className="form-label" htmlFor="deliveryAddress">Delivery Address *</label>
            <input
              id="deliveryAddress"
              name="deliveryAddress"
              type="text"
              className={`form-input ${fieldErrors.customerAddress || fieldErrors.deliveryAddress ? 'error' : ''}`}
              value={customer.deliveryAddress}
              onChange={handleCustomerChange}
              placeholder="e.g. 123 MG Road, Pune, Maharashtra"
              required
            />
            {(fieldErrors.customerAddress || fieldErrors.deliveryAddress) && (
              <span className="form-error">{fieldErrors.customerAddress || fieldErrors.deliveryAddress}</span>
            )}
          </div>
        </div>

        {/* Line Items */}
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginTop: 'var(--space-2)' }}>
          <h4 style={{ borderBottom: 'none' }}>Order Line Items ({items.length})</h4>
          <button
            type="button"
            className="btn btn-ghost btn-sm"
            onClick={handleAddSamplePreset}
            style={{ fontSize: '11px', color: 'var(--brand)' }}
          >
            ⚡ Sample Items
          </button>
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-3)' }}>
          {fieldErrors.items && <span className="form-error">{fieldErrors.items}</span>}
          {items.map((item, idx) => {
            const productError = fieldErrors[`items[${idx}].productName`] || fieldErrors[`items.${idx}.productName`];
            const qtyError = fieldErrors[`items[${idx}].quantity`] || fieldErrors[`items.${idx}.quantity`];
            const priceError = fieldErrors[`items[${idx}].unitPrice`] || fieldErrors[`items.${idx}.unitPrice`];

            return (
              <div
                key={idx}
                style={{
                  background: 'var(--surface)',
                  border: productError || qtyError || priceError ? '1px solid var(--danger)' : '1px solid var(--border)',
                  borderRadius: 'var(--radius)',
                  padding: 'var(--space-3)',
                  display: 'flex',
                  flexDirection: 'column',
                  gap: 'var(--space-2)',
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <span style={{ fontSize: '11px', fontWeight: 600, color: 'var(--text-muted)' }}>
                    ITEM #{idx + 1}
                  </span>
                  {items.length > 1 && (
                    <button
                      type="button"
                      className="btn btn-ghost btn-sm"
                      onClick={() => handleRemoveItem(idx)}
                      style={{ color: 'var(--danger)', padding: '2px 6px', fontSize: '11px' }}
                    >
                      Remove
                    </button>
                  )}
                </div>

                <div className="form-group" style={{ marginBottom: 0 }}>
                  <input
                    type="text"
                    className={`form-input ${productError ? 'error' : ''}`}
                    placeholder="Product Name (e.g. Mechanical Keyboard)"
                    value={item.productName}
                    onChange={(e) => handleItemChange(idx, 'productName', e.target.value)}
                    required
                  />
                  {productError && <span className="form-error">{productError}</span>}
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 'var(--space-2)' }}>
                  <div className="form-group" style={{ marginBottom: 0 }}>
                    <label className="form-label" style={{ fontSize: '11px' }}>Qty</label>
                    <input
                      type="number"
                      min="1"
                      className={`form-input ${qtyError ? 'error' : ''}`}
                      value={item.quantity}
                      onChange={(e) => handleItemChange(idx, 'quantity', e.target.value)}
                      required
                    />
                    {qtyError && <span className="form-error">{qtyError}</span>}
                  </div>
                  <div className="form-group" style={{ marginBottom: 0 }}>
                    <label className="form-label" style={{ fontSize: '11px' }}>Unit Price (₹)</label>
                    <input
                      type="number"
                      min="0"
                      step="0.01"
                      className={`form-input ${priceError ? 'error' : ''}`}
                      placeholder="0.00"
                      value={item.unitPrice}
                      onChange={(e) => handleItemChange(idx, 'unitPrice', e.target.value)}
                      required
                    />
                    {priceError && <span className="form-error">{priceError}</span>}
                  </div>
                </div>
              </div>
            );
          })}

          <button
            type="button"
            className="btn btn-outline btn-sm"
            onClick={handleAddItem}
            style={{ width: '100%', justifyContent: 'center' }}
          >
            + Add Another Item
          </button>
        </div>

        {/* Live Calculation Preview */}
        <div className="totals-block" style={{ marginTop: 'var(--space-2)' }}>
          <div className="totals-row">
            <span className="text-secondary">Subtotal</span>
            <span>{formatCurrency(subtotal)}</span>
          </div>
          <div className="totals-row">
            <span className="text-secondary">GST (18%)</span>
            <span>{formatCurrency(tax)}</span>
          </div>
          <div className="totals-row total">
            <span>Total Payable</span>
            <span style={{ color: 'var(--brand)' }}>{formatCurrency(total)}</span>
          </div>
        </div>
      </form>
    </Drawer>
  );
};

export default CreateOrderDrawer;
