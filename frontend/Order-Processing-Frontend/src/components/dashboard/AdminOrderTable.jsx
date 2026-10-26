import React, { useState } from 'react';
import StatusBadge from '../common/StatusBadge';
import Spinner from '../common/Spinner';
import EmptyState from '../common/EmptyState';
import { formatCurrency } from '../orders/OrderItemsTable';
import { updateOrderStatus, downloadInvoice } from '../../api/orders';

export const AdminOrderTable = ({
  orders = [],
  totalElements = 0,
  page = 0,
  size = 10,
  totalPages = 1,
  onPageChange,
  onSizeChange,
  statusFilter,
  onStatusFilterChange,
  loading,
  selectedOrderId,
  onSelectOrder,
  onOrderUpdated,
  onRefresh,
}) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [actionLoadingId, setActionLoadingId] = useState(null);

  const filteredOrders = orders.filter((order) => {
    if (!searchQuery) return true;
    const q = searchQuery.toLowerCase();
    return (
      order.id.toLowerCase().includes(q) ||
      (order.customerEmail && order.customerEmail.toLowerCase().includes(q))
    );
  });

  const getQuickAction = (status) => {
    switch (status) {
      case 'PENDING':
        return { next: 'CONFIRMED', label: '✓ Confirm', style: 'btn-outline' };
      case 'CONFIRMED':
        return { next: 'SHIPPED', label: '🚚 Ship', style: 'btn-outline' };
      case 'SHIPPED':
        return { next: 'DELIVERED', label: '📦 Deliver', style: 'btn-outline' };
      default:
        return null;
    }
  };

  const handleQuickStatusChange = async (e, orderId, nextStatus) => {
    e.stopPropagation();
    try {
      setActionLoadingId(orderId);
      const updated = await updateOrderStatus(orderId, nextStatus);
      if (onOrderUpdated) {
        onOrderUpdated(updated);
      }
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to update order status');
    } finally {
      setActionLoadingId(null);
    }
  };

  const handleDownloadInvoice = async (e, orderId) => {
    e.stopPropagation();
    try {
      setActionLoadingId(orderId);
      await downloadInvoice(orderId);
    } catch (err) {
      alert(err.response?.data?.message || 'Invoice download failed');
    } finally {
      setActionLoadingId(null);
    }
  };

  return (
    <div className="card">
      {/* Table Toolbar */}
      <div className="card-header" style={{ flexWrap: 'wrap', gap: 'var(--space-3)' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)', flex: 1, minWidth: '240px' }}>
          <input
            type="text"
            className="form-input"
            placeholder="Search by Order ID or Email..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            style={{ maxWidth: '320px', padding: '6px 12px', fontSize: '13px' }}
          />

          <select
            className="form-select"
            value={statusFilter || ''}
            onChange={(e) => onStatusFilterChange(e.target.value || null)}
            style={{ width: 'auto', padding: '6px 12px', fontSize: '13px' }}
          >
            <option value="">All Statuses</option>
            <option value="PENDING">Pending</option>
            <option value="CONFIRMED">Confirmed</option>
            <option value="SHIPPED">Shipped</option>
            <option value="DELIVERED">Delivered</option>
            <option value="CANCELLED">Cancelled</option>
          </select>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
          <button
            type="button"
            className="btn btn-ghost btn-sm"
            onClick={onRefresh}
            title="Reload orders"
          >
            ↻ Refresh
          </button>
        </div>
      </div>

      {/* Table Content */}
      {loading ? (
        <div style={{ padding: 'var(--space-12)' }}>
          <Spinner size="lg" message="Loading orders..." />
        </div>
      ) : filteredOrders.length === 0 ? (
        <EmptyState
          title="No orders found"
          description={searchQuery || statusFilter ? 'Try modifying your search query or status filter.' : 'No orders have been placed in the system.'}
        />
      ) : (
        <div style={{ overflowX: 'auto' }}>
          <table className="data-table">
            <thead>
              <tr>
                <th>Order ID</th>
                <th>Date</th>
                <th>Customer Email</th>
                <th className="text-right">Items</th>
                <th className="text-right">Total Amount</th>
                <th>Status</th>
                <th className="text-right">Quick Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredOrders.map((order) => {
                const isSelected = order.id === selectedOrderId;
                const quickAction = getQuickAction(order.status);
                const canInvoice = order.status !== 'PENDING' && order.status !== 'CANCELLED';
                const isBusy = actionLoadingId === order.id;

                const dateStr = new Date(order.createdAt).toLocaleDateString('en-IN', {
                  day: '2-digit',
                  month: 'short',
                  year: 'numeric',
                  hour: '2-digit',
                  minute: '2-digit',
                });

                return (
                  <tr
                    key={order.id}
                    className={isSelected ? 'active' : ''}
                    onClick={() => onSelectOrder(order.id)}
                    title="Click to view complete details"
                  >
                    <td>
                      <span style={{ fontFamily: 'monospace', fontWeight: 600 }}>
                        #{order.id.substring(0, 8)}...
                      </span>
                    </td>
                    <td style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>
                      {dateStr}
                    </td>
                    <td>
                      <span style={{ fontWeight: 500 }}>{order.customerEmail}</span>
                    </td>
                    <td className="text-right">{order.itemCount}</td>
                    <td className="text-right" style={{ fontWeight: 600 }}>
                      {formatCurrency(order.total)}
                    </td>
                    <td>
                      <StatusBadge status={order.status} />
                    </td>
                    <td className="text-right">
                      <div style={{ display: 'inline-flex', alignItems: 'center', gap: 'var(--space-2)' }} onClick={(e) => e.stopPropagation()}>
                        {quickAction && (
                          <button
                            type="button"
                            className={`btn btn-sm ${quickAction.style}`}
                            onClick={(e) => handleQuickStatusChange(e, order.id, quickAction.next)}
                            disabled={isBusy}
                            style={{ fontSize: '11px', padding: '3px 8px' }}
                          >
                            {isBusy ? '...' : quickAction.label}
                          </button>
                        )}

                        {canInvoice && (
                          <button
                            type="button"
                            className="btn btn-ghost btn-sm"
                            onClick={(e) => handleDownloadInvoice(e, order.id)}
                            disabled={isBusy}
                            title="Download invoice PDF"
                            style={{ fontSize: '11px', padding: '3px 8px' }}
                          >
                            📄 PDF
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}

      {/* Pagination Bar */}
      <div style={{
        padding: 'var(--space-3) var(--space-6)',
        borderTop: '1px solid var(--border)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        fontSize: '12px',
        color: 'var(--text-secondary)',
      }}>
        <div>
          Showing {filteredOrders.length} of {totalElements} orders
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-1)' }}>
            <span>Rows per page:</span>
            <select
              className="form-select"
              style={{ width: 'auto', padding: '2px 6px', fontSize: '12px' }}
              value={size}
              onChange={(e) => onSizeChange(Number(e.target.value))}
            >
              <option value={10}>10</option>
              <option value={20}>20</option>
              <option value={50}>50</option>
            </select>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-1)' }}>
            <button
              type="button"
              className="btn btn-ghost btn-sm"
              disabled={page <= 0}
              onClick={() => onPageChange(page - 1)}
              style={{ padding: '2px 8px' }}
            >
              ← Prev
            </button>
            <span style={{ fontWeight: 600, color: 'var(--text-primary)' }}>
              Page {page + 1} of {Math.max(1, totalPages)}
            </span>
            <button
              type="button"
              className="btn btn-ghost btn-sm"
              disabled={page >= totalPages - 1}
              onClick={() => onPageChange(page + 1)}
              style={{ padding: '2px 8px' }}
            >
              Next →
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminOrderTable;
