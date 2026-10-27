import React, { useState } from 'react';
import StatusBadge from '../common/StatusBadge';
import Spinner from '../common/Spinner';
import OrderTimeline from './OrderTimeline';
import OrderItemsTable, { formatCurrency } from './OrderItemsTable';
import { downloadInvoice, updateOrderStatus } from '../../api/orders';
import { useAuth } from '../../context/AuthContext';
import { extractApiError } from '../../api/errorUtils';

/**
 * Right-pane Order Detail View.
 * Displays tracking timeline, line items, customer details, and invoice download.
 * For ADMIN, provides an inline status transition dropdown.
 */
export const OrderDetailPanel = ({ order, loading, error, onStatusUpdated }) => {
  const { isAdmin } = useAuth();
  const [downloading, setDownloading] = useState(false);
  const [updatingStatus, setUpdatingStatus] = useState(false);
  const [statusError, setStatusError] = useState('');
  const [copied, setCopied] = useState(false);

  if (loading) {
    return (
      <div className="pane-right loading-center" style={{ minHeight: '400px' }}>
        <Spinner size="lg" message="Loading order details..." />
      </div>
    );
  }

  if (error) {
    return (
      <div className="pane-right" style={{ padding: 'var(--space-8)' }}>
        <div className="alert alert-error">{error}</div>
      </div>
    );
  }

  if (!order) {
    return (
      <div className="pane-right loading-center" style={{ minHeight: '400px' }}>
        <div style={{ textAlign: 'center', color: 'var(--text-muted)' }}>
          <div style={{ fontSize: '40px', marginBottom: 'var(--space-2)' }}>📋</div>
          <h3 style={{ color: 'var(--text-secondary)' }}>Select an order</h3>
          <p style={{ fontSize: '13px' }}>Click any order on the left to view full timeline and details.</p>
        </div>
      </div>
    );
  }

  const handleCopyId = () => {
    navigator.clipboard.writeText(order.id);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleDownloadInvoice = async () => {
    try {
      setDownloading(true);
      await downloadInvoice(order.id);
    } catch (err) {
      const parsed = await extractApiError(err, 'Invoice download failed.');
      alert(parsed.message);
    } finally {
      setDownloading(false);
    }
  };

  const handleStatusChange = async (e) => {
    const newStatus = e.target.value;
    if (!newStatus || newStatus === order.status) return;

    try {
      setUpdatingStatus(true);
      setStatusError('');
      const updated = await updateOrderStatus(order.id, newStatus);
      if (onStatusUpdated) {
        onStatusUpdated(updated);
      }
    } catch (err) {
      const parsed = await extractApiError(err, 'Failed to update order status');
      setStatusError(parsed.message);
    } finally {
      setUpdatingStatus(false);
    }
  };

  // Backend forbids invoice generation for PENDING and CANCELLED orders
  const canDownloadInvoice = order.status !== 'PENDING' && order.status !== 'CANCELLED';

  const validNextTransitions = {
    PENDING: ['CONFIRMED', 'CANCELLED'],
    CONFIRMED: ['SHIPPED', 'CANCELLED'],
    SHIPPED: ['DELIVERED'],
    DELIVERED: [],
    CANCELLED: [],
  };

  const availableTransitions = validNextTransitions[order.status] || [];

  return (
    <div className="pane-right" style={{ padding: 'var(--space-6)' }}>
      {/* Top Header Card */}
      <div className="card" style={{ marginBottom: 'var(--space-5)' }}>
        <div className="card-header" style={{ flexWrap: 'wrap', gap: 'var(--space-3)' }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
              <h2 style={{ fontSize: '16px', fontFamily: 'monospace' }}>
                Order #{order.id?.substring(0, 8)}...
              </h2>
              <button
                type="button"
                className="btn btn-ghost btn-sm"
                onClick={handleCopyId}
                title="Copy full UUID"
                style={{ fontSize: '11px', padding: '2px 6px' }}
              >
                {copied ? '✓ Copied' : '📋 Copy ID'}
              </button>
            </div>
            <p className="text-secondary text-sm" style={{ marginTop: '2px' }}>
              Placed on {new Date(order.createdAt).toLocaleDateString('en-IN', {
                day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit'
              })}
            </p>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)' }}>
            <StatusBadge status={order.status} />

            {/* Invoice Download Button */}
            <button
              type="button"
              className="btn btn-outline btn-sm"
              onClick={handleDownloadInvoice}
              disabled={!canDownloadInvoice || downloading}
              title={canDownloadInvoice ? 'Download PDF invoice' : 'Invoice available once order is confirmed'}
            >
              {downloading ? 'Downloading...' : '📄 Download Invoice'}
            </button>
          </div>
        </div>

        {/* Admin Status Transition Control */}
        {isAdmin && availableTransitions.length > 0 && (
          <div style={{
            padding: 'var(--space-3) var(--space-6)',
            background: 'var(--surface)',
            borderTop: '1px solid var(--border)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            gap: 'var(--space-4)',
          }}>
            <span style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text-secondary)' }}>
              Update Order Status (Admin):
            </span>
            <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
              <select
                className="form-select"
                style={{ width: 'auto', padding: '4px 10px', fontSize: '12px' }}
                value={order.status}
                onChange={handleStatusChange}
                disabled={updatingStatus}
              >
                <option value={order.status}>Current: {order.status}</option>
                {availableTransitions.map((st) => (
                  <option key={st} value={st}>
                    Move to {st} →
                  </option>
                ))}
              </select>
              {updatingStatus && <Spinner size="sm" center={false} />}
            </div>
          </div>
        )}

        {statusError && (
          <div style={{ padding: 'var(--space-2) var(--space-6)' }}>
            <span className="form-error">{statusError}</span>
          </div>
        )}
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 'var(--space-5)', marginBottom: 'var(--space-5)' }}>
        {/* Customer & Delivery Information */}
        <div className="card">
          <div className="card-header">
            <h3>Delivery & Customer Details</h3>
          </div>
          <div className="card-body" style={{ fontSize: '13px', display: 'flex', flexDirection: 'column', gap: 'var(--space-2)' }}>
            <div>
              <span className="text-secondary" style={{ display: 'block', fontSize: '11px', textTransform: 'uppercase', letterSpacing: '0.04em' }}>Customer Name</span>
              <strong style={{ color: 'var(--text-primary)' }}>{order.customer?.name || 'N/A'}</strong>
            </div>
            <div>
              <span className="text-secondary" style={{ display: 'block', fontSize: '11px', textTransform: 'uppercase', letterSpacing: '0.04em' }}>Email</span>
              <span>{order.customer?.email || 'N/A'}</span>
            </div>
            <div>
              <span className="text-secondary" style={{ display: 'block', fontSize: '11px', textTransform: 'uppercase', letterSpacing: '0.04em' }}>Phone</span>
              <span>{order.customer?.phone || 'Not provided'}</span>
            </div>
            <div>
              <span className="text-secondary" style={{ display: 'block', fontSize: '11px', textTransform: 'uppercase', letterSpacing: '0.04em' }}>Delivery Address</span>
              <span>{order.customer?.address || 'Standard Delivery'}</span>
            </div>
          </div>
        </div>

        {/* Tracking Timeline */}
        <div className="card">
          <div className="card-header">
            <h3>Tracking Timeline</h3>
          </div>
          <div className="card-body">
            <OrderTimeline
              status={order.status}
              createdAt={order.createdAt}
              updatedAt={order.updatedAt}
            />
          </div>
        </div>
      </div>

      {/* Order Line Items */}
      <div className="card">
        <div className="card-header">
          <h3>Order Items & Summary ({order.items?.length || 0})</h3>
        </div>
        <div className="card-body">
          <OrderItemsTable
            items={order.items}
            subtotal={order.subtotal}
            taxAmount={order.taxAmount}
            total={order.total}
            taxRate={order.taxRate}
          />
        </div>
      </div>
    </div>
  );
};

export default OrderDetailPanel;
