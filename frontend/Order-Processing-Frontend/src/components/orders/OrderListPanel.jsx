import React, { useState } from 'react';
import StatusBadge from '../common/StatusBadge';
import Spinner from '../common/Spinner';
import EmptyState from '../common/EmptyState';
import { formatCurrency } from './OrderItemsTable';

/**
 * Left-pane Order List showing compact order cards.
 *
 * @param {Array} orders - List of OrderSummaryResponse
 * @param {string} selectedOrderId - Currently selected order UUID
 * @param {Function} onSelectOrder - Callback when user clicks an order
 * @param {boolean} loading - Loading state indicator
 * @param {Function} onRefresh - Callback to refresh order list
 * @param {Function} onCreateOrder - Callback to open create order drawer
 */
export const OrderListPanel = ({
  orders = [],
  selectedOrderId,
  onSelectOrder,
  loading,
  onRefresh,
  onCreateOrder,
}) => {
  const [filterQuery, setFilterQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');

  const filteredOrders = orders.filter((order) => {
    const matchesQuery =
      order.id.toLowerCase().includes(filterQuery.toLowerCase()) ||
      (order.customerEmail && order.customerEmail.toLowerCase().includes(filterQuery.toLowerCase()));

    const matchesStatus = statusFilter === 'ALL' || order.status === statusFilter;

    return matchesQuery && matchesStatus;
  });

  return (
    <aside className="pane-left">
      {/* Top Search & Filter Bar */}
      <div style={{
        padding: 'var(--space-4)',
        borderBottom: '1px solid var(--border)',
        background: 'var(--surface)',
        display: 'flex',
        flexDirection: 'column',
        gap: 'var(--space-2)',
      }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <span style={{ fontWeight: 600, fontSize: '13px', color: 'var(--text-primary)' }}>
            Orders ({orders.length})
          </span>
          <button
            type="button"
            className="btn btn-ghost btn-sm"
            onClick={onRefresh}
            title="Refresh order list"
            style={{ padding: '2px 6px', fontSize: '11px' }}
          >
            ↻ Refresh
          </button>
        </div>

        <input
          type="text"
          className="form-input"
          placeholder="Filter by Order ID or Email..."
          value={filterQuery}
          onChange={(e) => setFilterQuery(e.target.value)}
          style={{ fontSize: '12px', padding: '6px 10px' }}
        />

        <div style={{ display: 'flex', gap: '4px', overflowX: 'auto', paddingBottom: '2px' }}>
          {['ALL', 'PENDING', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELLED'].map((st) => (
            <button
              key={st}
              type="button"
              className="btn btn-ghost btn-sm"
              onClick={() => setStatusFilter(st)}
              style={{
                fontSize: '10px',
                padding: '2px 6px',
                fontWeight: statusFilter === st ? 700 : 400,
                color: statusFilter === st ? 'var(--brand)' : 'var(--text-secondary)',
                background: statusFilter === st ? 'var(--brand-light)' : 'transparent',
                border: statusFilter === st ? '1px solid var(--brand-border)' : '1px solid transparent',
              }}
            >
              {st}
            </button>
          ))}
        </div>
      </div>

      {/* Orders List Container */}
      {loading ? (
        <div style={{ padding: 'var(--space-8)' }}>
          <Spinner message="Loading orders..." />
        </div>
      ) : filteredOrders.length === 0 ? (
        <EmptyState
          title="No orders found"
          description={filterQuery || statusFilter !== 'ALL' ? 'Try clearing your filters.' : 'You have not placed any orders yet.'}
          action={
            onCreateOrder ? (
              <button
                type="button"
                className="btn btn-primary btn-sm"
                onClick={onCreateOrder}
              >
                + Create Order
              </button>
            ) : null
          }
        />
      ) : (
        <div>
          {filteredOrders.map((order) => {
            const isSelected = order.id === selectedOrderId;
            const shortId = order.id.substring(0, 8);
            const dateStr = new Date(order.createdAt).toLocaleDateString('en-IN', {
              day: '2-digit',
              month: 'short',
            });

            return (
              <div
                key={order.id}
                className={`order-list-item ${isSelected ? 'active' : ''}`}
                onClick={() => onSelectOrder(order.id)}
              >
                <div className="order-list-item-top">
                  <span className="order-list-item-id">#{shortId}</span>
                  <StatusBadge status={order.status} />
                </div>

                <div className="order-list-item-meta">
                  <span>{dateStr}</span>
                  <span> · </span>
                  <span>{order.itemCount} {order.itemCount === 1 ? 'item' : 'items'}</span>
                </div>

                <div className="order-list-item-bottom">
                  <span className="order-total">{formatCurrency(order.total)}</span>
                  <span style={{ fontSize: '11px', color: 'var(--text-muted)' }}>
                    {order.customerEmail}
                  </span>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </aside>
  );
};

export default OrderListPanel;
