import React from 'react';

/**
 * Format number as Indian currency (e.g. ₹50,000.00).
 */
export const formatCurrency = (amount) => {
  if (amount == null) return '₹0.00';
  const num = typeof amount === 'number' ? amount : parseFloat(amount);
  if (isNaN(num)) return '₹0.00';
  return '₹' + num.toLocaleString('en-IN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });
};

/**
 * Order line items table and financial breakdown.
 *
 * @param {Array} items - List of OrderItemResponse: [{ id, productName, quantity, unitPrice, lineTotal }]
 * @param {number|string} subtotal
 * @param {number|string} taxAmount
 * @param {number|string} total
 * @param {number|string} [taxRate=0.18]
 */
export const OrderItemsTable = ({ items = [], subtotal, taxAmount, total, taxRate = 0.18 }) => {
  const taxPercent = (parseFloat(taxRate) * 100).toFixed(0);

  return (
    <div className="order-items-container">
      <table className="items-table">
        <thead>
          <tr>
            <th>Product</th>
            <th className="text-right">Qty</th>
            <th className="text-right">Unit Price</th>
            <th className="text-right">Total</th>
          </tr>
        </thead>
        <tbody>
          {items.map((item, index) => (
            <tr key={item.id || index}>
              <td>
                <span style={{ fontWeight: 600, color: 'var(--text-primary)' }}>
                  {item.productName}
                </span>
              </td>
              <td className="text-right">{item.quantity}</td>
              <td className="text-right">{formatCurrency(item.unitPrice)}</td>
              <td className="text-right" style={{ fontWeight: 600 }}>
                {formatCurrency(item.lineTotal)}
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      <div className="totals-block" style={{ marginTop: 'var(--space-4)' }}>
        <div className="totals-row">
          <span className="text-secondary">Subtotal</span>
          <span>{formatCurrency(subtotal)}</span>
        </div>
        <div className="totals-row">
          <span className="text-secondary">GST ({taxPercent}%)</span>
          <span>{formatCurrency(taxAmount)}</span>
        </div>
        <div className="totals-row total">
          <span>Total Amount</span>
          <span style={{ color: 'var(--brand)' }}>{formatCurrency(total)}</span>
        </div>
      </div>
    </div>
  );
};

export default OrderItemsTable;
