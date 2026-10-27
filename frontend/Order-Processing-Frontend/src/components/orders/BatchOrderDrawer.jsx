import React, { useState, useEffect } from 'react';
import Drawer from '../common/Drawer';
import { useAuth } from '../../context/AuthContext';
import { createOrder } from '../../api/orders';
import { formatCurrency } from './OrderItemsTable';

export const BatchOrderDrawer = ({ isOpen, onClose, onBatchCreated }) => {
  const { user } = useAuth();

  const getInitialPresets = () => [
    {
      id: 'pkg-1',
      title: 'Executive Workstation',
      enabled: true,
      customerName: user?.name || 'Pravin Mali',
      customerEmail: user?.email || 'pravin@example.com',
      customerPhone: '+91 98765 43210',
      customerAddress: user?.address || 'FC Road, Pune, Maharashtra - 411004',
      items: [
        { productName: 'Apple MacBook Pro 16" M3 Max', quantity: 1, unitPrice: '249900' },
        { productName: 'Dell UltraSharp 32" 4K USB-C Monitor', quantity: 1, unitPrice: '68900' },
      ],
    },
    {
      id: 'pkg-2',
      title: 'Audio & Wireless Peripherals',
      enabled: true,
      customerName: user?.name || 'Pravin Mali',
      customerEmail: user?.email || 'pravin@example.com',
      customerPhone: '+91 98765 43210',
      customerAddress: user?.address || 'FC Road, Pune, Maharashtra - 411004',
      items: [
        { productName: 'Sony WH-1000XM5 Wireless Headphones', quantity: 1, unitPrice: '26990' },
        { productName: 'Logitech MX Master 3S Ergonomic Mouse', quantity: 2, unitPrice: '7995' },
      ],
    },
    {
      id: 'pkg-3',
      title: 'Desk Ergonomics Setup',
      enabled: true,
      customerName: user?.name || 'Pravin Mali',
      customerEmail: user?.email || 'pravin@example.com',
      customerPhone: '+91 98765 43210',
      customerAddress: user?.address || 'FC Road, Pune, Maharashtra - 411004',
      items: [
        { productName: 'Keychron K2 Mechanical Keyboard', quantity: 1, unitPrice: '8499' },
        { productName: 'Aluminum Adjustable Laptop Stand', quantity: 1, unitPrice: '2499' },
      ],
    },
  ];

  const [presets, setPresets] = useState(getInitialPresets);
  const [submitting, setSubmitting] = useState(false);
  const [progressMsg, setProgressMsg] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    if (isOpen) {
      setPresets(getInitialPresets());
      setError('');
      setProgressMsg('');
    }
  }, [isOpen, user]);

  const handleToggleEnable = (idx) => {
    setPresets((prev) => {
      const copy = [...prev];
      copy[idx] = { ...copy[idx], enabled: !copy[idx].enabled };
      return copy;
    });
  };

  const handleFieldChange = (idx, field, value) => {
    setPresets((prev) => {
      const copy = [...prev];
      copy[idx] = { ...copy[idx], [field]: value };
      return copy;
    });
  };

  const handleItemChange = (pkgIdx, itemIdx, field, value) => {
    setPresets((prev) => {
      const copy = [...prev];
      const items = [...copy[pkgIdx].items];
      items[itemIdx] = { ...items[itemIdx], [field]: value };
      copy[pkgIdx] = { ...copy[pkgIdx], items };
      return copy;
    });
  };

  const handleAddItem = (pkgIdx) => {
    setPresets((prev) => {
      const copy = [...prev];
      copy[pkgIdx] = {
        ...copy[pkgIdx],
        items: [...copy[pkgIdx].items, { productName: '', quantity: 1, unitPrice: '' }],
      };
      return copy;
    });
  };

  const handleRemoveItem = (pkgIdx, itemIdx) => {
    setPresets((prev) => {
      const copy = [...prev];
      if (copy[pkgIdx].items.length <= 1) return prev;
      copy[pkgIdx] = {
        ...copy[pkgIdx],
        items: copy[pkgIdx].items.filter((_, i) => i !== itemIdx),
      };
      return copy;
    });
  };

  // Calculate order level totals
  const calculateOrderTotal = (order) => {
    let subtotal = 0;
    order.items.forEach((it) => {
      const q = parseInt(it.quantity, 10) || 0;
      const p = parseFloat(it.unitPrice) || 0;
      subtotal += q * p;
    });
    const tax = subtotal * 0.18;
    return subtotal + tax;
  };

  const selectedOrders = presets.filter((p) => p.enabled);
  const grandTotal = selectedOrders.reduce((sum, ord) => sum + calculateOrderTotal(ord), 0);

  const handleSubmitBatch = async () => {
    if (selectedOrders.length === 0) {
      setError('Please select at least one order to create.');
      return;
    }

    // Validation
    for (const ord of selectedOrders) {
      if (!ord.customerName.trim() || !ord.customerEmail.trim()) {
        setError(`Please ensure customer name & email are provided for "${ord.title}".`);
        return;
      }
      if (!ord.customerAddress.trim()) {
        setError(`Delivery address is required for "${ord.title}".`);
        return;
      }
      if (ord.items.length === 0) {
        setError(`"${ord.title}" must contain at least one item.`);
        return;
      }
      for (let i = 0; i < ord.items.length; i++) {
        const it = ord.items[i];
        if (!it.productName.trim()) {
          setError(`Product name is required for item #${i + 1} in "${ord.title}".`);
          return;
        }
        const q = parseInt(it.quantity, 10);
        const p = parseFloat(it.unitPrice);
        if (isNaN(q) || q < 1) {
          setError(`Item #${i + 1} in "${ord.title}" must have quantity >= 1.`);
          return;
        }
        if (isNaN(p) || p <= 0) {
          setError(`Item #${i + 1} in "${ord.title}" must have a valid price.`);
          return;
        }
      }
    }

    try {
      setSubmitting(true);
      setError('');

      for (let i = 0; i < selectedOrders.length; i++) {
        const ord = selectedOrders[i];
        setProgressMsg(`Creating order ${i + 1} of ${selectedOrders.length}: "${ord.title}"...`);

        const payload = {
          customerName: ord.customerName.trim(),
          customerEmail: ord.customerEmail.trim(),
          customerPhone: ord.customerPhone.trim() || null,
          customerAddress: ord.customerAddress.trim(),
          items: ord.items.map((it) => ({
            productName: it.productName.trim(),
            quantity: parseInt(it.quantity, 10),
            unitPrice: parseFloat(it.unitPrice),
          })),
        };

        await createOrder(payload);
      }

      setProgressMsg('All orders created successfully!');
      setTimeout(() => {
        onClose();
        if (onBatchCreated) {
          onBatchCreated();
        }
      }, 500);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed during batch order creation.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Drawer
      isOpen={isOpen}
      onClose={onClose}
      title="⚡ Batch Order Generator"
      footer={
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', width: '100%' }}>
          <div>
            <span style={{ fontSize: '11px', color: 'var(--text-muted)', display: 'block' }}>
              {selectedOrders.length} {selectedOrders.length === 1 ? 'order' : 'orders'} selected
            </span>
            <strong style={{ fontSize: '15px', color: 'var(--brand)' }}>
              {formatCurrency(grandTotal)}
            </strong>
          </div>

          <div style={{ display: 'flex', gap: 'var(--space-2)' }}>
            <button
              type="button"
              className="btn btn-ghost"
              onClick={onClose}
              disabled={submitting}
            >
              Cancel
            </button>
            <button
              type="button"
              className="btn btn-primary"
              onClick={handleSubmitBatch}
              disabled={submitting || selectedOrders.length === 0}
            >
              {submitting ? 'Creating Orders...' : `Place ${selectedOrders.length} Selected Orders →`}
            </button>
          </div>
        </div>
      }
    >
      <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
        <div style={{
          background: 'var(--brand-light)',
          border: '1px solid var(--brand-border)',
          borderRadius: 'var(--radius)',
          padding: 'var(--space-3)',
          fontSize: '12px',
          color: '#1E40AF',
        }}>
          💡 <strong>Review & Edit Presets:</strong> Inspect or modify any recipient, address, and product details below. Check the boxes of the orders you want to generate.
        </div>

        {error && <div className="alert alert-error">{error}</div>}
        {progressMsg && <div className="alert alert-success">{progressMsg}</div>}

        {presets.map((pkg, pkgIdx) => {
          const totalVal = calculateOrderTotal(pkg);

          return (
            <div
              key={pkg.id}
              style={{
                border: pkg.enabled ? '2px solid var(--brand-border)' : '1px solid var(--border)',
                borderRadius: 'var(--radius-lg)',
                background: pkg.enabled ? 'var(--card)' : 'var(--surface)',
                overflow: 'hidden',
                transition: 'all var(--transition)',
              }}
            >
              {/* Card Header with Checkbox */}
              <div
                style={{
                  padding: 'var(--space-3) var(--space-4)',
                  background: pkg.enabled ? 'var(--surface)' : 'rgba(0,0,0,0.02)',
                  borderBottom: '1px solid var(--border)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                }}
              >
                <label style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)', cursor: 'pointer' }}>
                  <input
                    type="checkbox"
                    checked={pkg.enabled}
                    onChange={() => handleToggleEnable(pkgIdx)}
                    style={{ cursor: 'pointer' }}
                  />
                  <strong style={{ fontSize: '14px', color: 'var(--text-primary)' }}>
                    Order #{pkgIdx + 1}: {pkg.title}
                  </strong>
                </label>

                <span style={{ fontSize: '13px', fontWeight: 600, color: pkg.enabled ? 'var(--brand)' : 'var(--text-muted)' }}>
                  {formatCurrency(totalVal)}
                </span>
              </div>

              {/* Card Form Body (only active if enabled) */}
              {pkg.enabled && (
                <div style={{ padding: 'var(--space-4)', display: 'flex', flexDirection: 'column', gap: 'var(--space-3)' }}>
                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 'var(--space-3)' }}>
                    <div className="form-group">
                      <label className="form-label" style={{ fontSize: '11px' }}>Customer Name *</label>
                      <input
                        type="text"
                        className="form-input"
                        value={pkg.customerName}
                        onChange={(e) => handleFieldChange(pkgIdx, 'customerName', e.target.value)}
                        style={{ fontSize: '12px', padding: '6px 10px' }}
                        required
                      />
                    </div>
                    <div className="form-group">
                      <label className="form-label" style={{ fontSize: '11px' }}>Customer Email *</label>
                      <input
                        type="email"
                        className="form-input"
                        value={pkg.customerEmail}
                        onChange={(e) => handleFieldChange(pkgIdx, 'customerEmail', e.target.value)}
                        style={{ fontSize: '12px', padding: '6px 10px' }}
                        required
                      />
                    </div>
                  </div>

                  <div className="form-group">
                    <label className="form-label" style={{ fontSize: '11px' }}>Delivery Address *</label>
                    <input
                      type="text"
                      className="form-input"
                      value={pkg.customerAddress}
                      onChange={(e) => handleFieldChange(pkgIdx, 'customerAddress', e.target.value)}
                      placeholder="e.g. 123 MG Road, Pune"
                      style={{ fontSize: '12px', padding: '6px 10px' }}
                      required
                    />
                  </div>

                  {/* Items List */}
                  <div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '4px' }}>
                      <span style={{ fontSize: '11px', fontWeight: 600, color: 'var(--text-muted)', textTransform: 'uppercase' }}>
                        Line Items ({pkg.items.length})
                      </span>
                      <button
                        type="button"
                        className="btn btn-ghost btn-sm"
                        onClick={() => handleAddItem(pkgIdx)}
                        style={{ fontSize: '11px', padding: '2px 6px', color: 'var(--brand)' }}
                      >
                        + Add Line
                      </button>
                    </div>

                    <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-2)' }}>
                      {pkg.items.map((it, itIdx) => (
                        <div
                          key={itIdx}
                          style={{
                            display: 'grid',
                            gridTemplateColumns: '1fr 60px 100px auto',
                            gap: 'var(--space-2)',
                            alignItems: 'center',
                          }}
                        >
                          <input
                            type="text"
                            className="form-input"
                            value={it.productName}
                            placeholder="Product name"
                            onChange={(e) => handleItemChange(pkgIdx, itIdx, 'productName', e.target.value)}
                            style={{ fontSize: '12px', padding: '5px 8px' }}
                            required
                          />
                          <input
                            type="number"
                            min="1"
                            className="form-input"
                            value={it.quantity}
                            placeholder="Qty"
                            onChange={(e) => handleItemChange(pkgIdx, itIdx, 'quantity', e.target.value)}
                            style={{ fontSize: '12px', padding: '5px 8px' }}
                            required
                          />
                          <input
                            type="number"
                            min="0"
                            step="0.01"
                            className="form-input"
                            value={it.unitPrice}
                            placeholder="Price"
                            onChange={(e) => handleItemChange(pkgIdx, itIdx, 'unitPrice', e.target.value)}
                            style={{ fontSize: '12px', padding: '5px 8px' }}
                            required
                          />
                          {pkg.items.length > 1 ? (
                            <button
                              type="button"
                              className="btn btn-ghost btn-sm"
                              onClick={() => handleRemoveItem(pkgIdx, itIdx)}
                              style={{ color: 'var(--danger)', padding: '2px 6px', fontSize: '12px' }}
                              title="Remove item"
                            >
                              ✕
                            </button>
                          ) : (
                            <div style={{ width: '20px' }} />
                          )}
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              )}
            </div>
          );
        })}
      </div>
    </Drawer>
  );
};

export default BatchOrderDrawer;
