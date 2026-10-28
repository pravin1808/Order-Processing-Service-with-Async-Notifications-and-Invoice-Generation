import React, { useState, useEffect } from 'react';
import AppLayout from '../components/layout/AppLayout';
import StatCard from '../components/dashboard/StatCard';
import AdminOrderTable from '../components/dashboard/AdminOrderTable';
import OrderDetailPanel from '../components/orders/OrderDetailPanel';
import BatchOrderDrawer from '../components/orders/BatchOrderDrawer';
import CreateOrderDrawer from '../components/orders/CreateOrderDrawer';
import { getOrders, getOrderById, exportOrdersCsv } from '../api/orders';
import { formatCurrency } from '../components/orders/OrderItemsTable';
import { extractApiError } from '../api/errorUtils';
import { useAuth } from '../context/AuthContext';

export const DashboardPage = () => {
  const { user, isAdmin, isCustomer } = useAuth();
  const [orders, setOrders] = useState([]);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [statusFilter, setStatusFilter] = useState(null);
  const [loading, setLoading] = useState(true);
  const [dashboardError, setDashboardError] = useState('');
  const [exporting, setExporting] = useState(false);
  const [isBatchOpen, setIsBatchOpen] = useState(false);
  const [isCreateOpen, setIsCreateOpen] = useState(false);

  // Selected Order for side inspection panel
  const [selectedOrderId, setSelectedOrderId] = useState(null);
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [detailLoading, setDetailLoading] = useState(false);
  const [detailError, setDetailError] = useState('');

  const fetchOrders = async (targetPage = page, targetSize = size, targetStatus = statusFilter) => {
    try {
      setLoading(true);
      setDashboardError('');
      const params = {
        page: targetPage,
        size: targetSize,
        sort: 'createdAt,desc',
      };
      if (targetStatus) {
        params.status = targetStatus;
      }

      const res = await getOrders(params);
      const list = res.content || [];
      setOrders(list);
      setTotalPages(res.totalPages || 1);
      setTotalElements(res.totalElements || 0);

      // Auto-select first order if none selected or current is out of range
      if (list.length > 0 && (!selectedOrderId || !list.some((o) => o.id === selectedOrderId))) {
        handleSelectOrder(list[0].id);
      }
    } catch (err) {
      const parsed = await extractApiError(err, 'Failed to load admin orders.');
      setDashboardError(parsed.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchOrders(page, size, statusFilter);
  }, [page, size, statusFilter]);

  const handleSelectOrder = async (orderId) => {
    setSelectedOrderId(orderId);
    try {
      setDetailLoading(true);
      setDetailError('');
      const full = await getOrderById(orderId);
      setSelectedOrder(full);
    } catch (err) {
      const parsed = await extractApiError(err, 'Failed to load order details');
      setDetailError(parsed.message);
      setSelectedOrder(null);
    } finally {
      setDetailLoading(false);
    }
  };

  const handleOrderUpdated = (updatedOrder) => {
    // Update in local table
    setOrders((prev) =>
      prev.map((o) => (o.id === updatedOrder.id ? { ...o, status: updatedOrder.status } : o))
    );
    if (selectedOrderId === updatedOrder.id) {
      setSelectedOrder(updatedOrder);
    }
  };

  const handleExportCsv = async () => {
    try {
      setExporting(true);
      await exportOrdersCsv();
    } catch (err) {
      const parsed = await extractApiError(err, 'Failed to export CSV. Please try again.');
      alert(parsed.message);
    } finally {
      setExporting(false);
    }
  };

  // Calculate quick dashboard stats
  const pendingCount = orders.filter((o) => o.status === 'PENDING').length;
  const inTransitCount = orders.filter((o) => o.status === 'CONFIRMED' || o.status === 'SHIPPED').length;
  const totalRevenue = orders.reduce((acc, curr) => acc + (parseFloat(curr.total) || 0), 0);

  return (
    <AppLayout
      onCreateOrderClick={() => setIsCreateOpen(true)}
      onOpenBatchDrawer={() => setIsBatchOpen(true)}
    >
      <div className="page" style={{ maxWidth: '1600px' }}>
        {dashboardError && (
          <div className="alert alert-error" style={{ marginBottom: 'var(--space-4)', whiteSpace: 'pre-line' }}>
            {dashboardError}
          </div>
        )}

        {/* Page Header */}
        <div className="page-header" style={{ flexWrap: 'wrap', gap: 'var(--space-3)' }}>
          <div>
            <h1>{isAdmin ? 'Admin Operations Dashboard' : `Customer Dashboard — Welcome, ${user?.name || 'Customer'}`}</h1>
            <p className="text-secondary text-sm">
              {isAdmin
                ? 'Real-time order fulfillment, customer status transitions, and accounting'
                : 'Overview of your personal orders, delivery tracking, and spending summary'}
            </p>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)' }}>
            {isCustomer && (
              <button
                type="button"
                className="btn btn-primary btn-sm"
                onClick={() => setIsCreateOpen(true)}
              >
                + Create Order
              </button>
            )}

            <button
              type="button"
              className="btn btn-outline btn-sm"
              onClick={() => setIsBatchOpen(true)}
            >
              ⚡ Batch Order Presets
            </button>

            {isAdmin && (
              <button
                type="button"
                className="btn btn-primary btn-sm"
                onClick={handleExportCsv}
                disabled={exporting}
              >
                {exporting ? 'Generating CSV...' : '📥 Export All Orders (CSV)'}
              </button>
            )}
          </div>
        </div>

        {/* Stats Grid */}
        <div className="stat-cards">
          <StatCard
            label="Total Orders"
            value={totalElements}
            sub={isAdmin ? 'Across all customer accounts' : 'Your total order history'}
            icon="📊"
          />
          <StatCard
            label="Pending Action"
            value={pendingCount}
            sub={isAdmin ? 'Needs fulfillment confirmation' : 'Awaiting confirmation'}
            icon="⏳"
            highlightColor="var(--status-pending-dot)"
          />
          <StatCard
            label="In Transit"
            value={inTransitCount}
            sub={isAdmin ? 'Confirmed & Out for delivery' : 'Confirmed & Shipped'}
            icon="🚚"
            highlightColor="var(--status-confirmed-dot)"
          />
          <StatCard
            label={isAdmin ? 'Batch Revenue' : 'Total Spending'}
            value={formatCurrency(totalRevenue)}
            sub={isAdmin ? 'Active view gross total' : 'Total value of listed orders'}
            icon="💳"
            highlightColor="var(--status-delivered-dot)"
          />
        </div>

        {/* Two Column Layout: Orders Table + Live Inspection Panel */}
        <div style={{
          display: 'grid',
          gridTemplateColumns: selectedOrder ? '1fr 480px' : '1fr',
          gap: 'var(--space-6)',
          alignItems: 'start',
        }}>
          {/* Main Orders Table */}
          <AdminOrderTable
            orders={orders}
            totalElements={totalElements}
            page={page}
            size={size}
            totalPages={totalPages}
            onPageChange={setPage}
            onSizeChange={(newSize) => {
              setSize(newSize);
              setPage(0);
            }}
            statusFilter={statusFilter}
            onStatusFilterChange={(st) => {
              setStatusFilter(st);
              setPage(0);
            }}
            loading={loading}
            selectedOrderId={selectedOrderId}
            onSelectOrder={handleSelectOrder}
            onOrderUpdated={handleOrderUpdated}
            onRefresh={() => fetchOrders(page, size, statusFilter)}
          />

          {/* Right Inspection Panel */}
          {selectedOrder && (
            <div style={{
              position: 'sticky',
              top: 'calc(var(--navbar-h) + var(--space-4))',
              maxHeight: 'calc(100vh - var(--navbar-h) - var(--space-8))',
              overflowY: 'auto',
            }}>
              <OrderDetailPanel
                order={selectedOrder}
                loading={detailLoading}
                error={detailError}
                onStatusUpdated={handleOrderUpdated}
              />
            </div>
          )}
        </div>
      </div>

      <BatchOrderDrawer
        isOpen={isBatchOpen}
        onClose={() => setIsBatchOpen(false)}
        onBatchCreated={() => fetchOrders(page, size, statusFilter)}
      />

      <CreateOrderDrawer
        isOpen={isCreateOpen}
        onClose={() => setIsCreateOpen(false)}
        onOrderCreated={() => fetchOrders(0, size, statusFilter)}
      />
    </AppLayout>
  );
};

export default DashboardPage;
