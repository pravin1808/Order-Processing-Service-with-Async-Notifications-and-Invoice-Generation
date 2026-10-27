import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import AppLayout from '../components/layout/AppLayout';
import OrderListPanel from '../components/orders/OrderListPanel';
import OrderDetailPanel from '../components/orders/OrderDetailPanel';
import CreateOrderDrawer from '../components/orders/CreateOrderDrawer';
import BatchOrderDrawer from '../components/orders/BatchOrderDrawer';
import { getOrders, getOrderById } from '../api/orders';
import { extractApiError } from '../api/errorUtils';

export const OrdersPage = () => {
  const { id: urlOrderId } = useParams();
  const navigate = useNavigate();

  const [orders, setOrders] = useState([]);
  const [listLoading, setListLoading] = useState(true);
  const [listError, setListError] = useState('');

  const [selectedOrderId, setSelectedOrderId] = useState(urlOrderId || null);
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [detailLoading, setDetailLoading] = useState(false);
  const [detailError, setDetailError] = useState('');

  const [isDrawerOpen, setIsDrawerOpen] = useState(false);
  const [isBatchDrawerOpen, setIsBatchDrawerOpen] = useState(false);

  // Load orders list
  const fetchOrders = async (preserveSelectedId = null) => {
    try {
      setListLoading(true);
      setListError('');
      const data = await getOrders({ page: 0, size: 50 });
      const orderList = data.content || [];
      setOrders(orderList);

      // Determine which order to select
      const targetId = preserveSelectedId || selectedOrderId || urlOrderId;
      if (targetId && orderList.some((o) => o.id === targetId)) {
        handleSelectOrder(targetId);
      } else if (orderList.length > 0) {
        handleSelectOrder(orderList[0].id);
      } else {
        setSelectedOrder(null);
        setSelectedOrderId(null);
      }
    } catch (err) {
      const parsed = await extractApiError(err, 'Failed to load orders.');
      setListError(parsed.message);
    } finally {
      setListLoading(false);
    }
  };

  useEffect(() => {
    fetchOrders();
  }, []);

  // Fetch full order details when selectedOrderId changes
  const handleSelectOrder = async (orderId) => {
    setSelectedOrderId(orderId);
    navigate(`/orders/${orderId}`, { replace: true });

    try {
      setDetailLoading(true);
      setDetailError('');
      const fullOrder = await getOrderById(orderId);
      setSelectedOrder(fullOrder);
    } catch (err) {
      const parsed = await extractApiError(err, 'Could not load order details');
      setDetailError(parsed.message);
      setSelectedOrder(null);
    } finally {
      setDetailLoading(false);
    }
  };

  // Callback when status is updated by admin
  const handleStatusUpdated = (updatedOrder) => {
    setSelectedOrder(updatedOrder);
    setOrders((prev) =>
      prev.map((ord) =>
        ord.id === updatedOrder.id ? { ...ord, status: updatedOrder.status } : ord
      )
    );
  };

  // Callback when a new order is created from the drawer
  const handleOrderCreated = (newOrder) => {
    fetchOrders(newOrder.id);
  };

  return (
    <AppLayout
      onCreateOrderClick={() => setIsDrawerOpen(true)}
      onOpenBatchDrawer={() => setIsBatchDrawerOpen(true)}
    >
      <div className="two-pane">
        <OrderListPanel
          orders={orders}
          selectedOrderId={selectedOrderId}
          onSelectOrder={handleSelectOrder}
          loading={listLoading}
          onRefresh={() => fetchOrders(selectedOrderId)}
          onCreateOrder={() => setIsDrawerOpen(true)}
        />

        <OrderDetailPanel
          order={selectedOrder}
          loading={detailLoading}
          error={detailError || listError}
          onStatusUpdated={handleStatusUpdated}
        />
      </div>

      <CreateOrderDrawer
        isOpen={isDrawerOpen}
        onClose={() => setIsDrawerOpen(false)}
        onOrderCreated={handleOrderCreated}
      />

      <BatchOrderDrawer
        isOpen={isBatchDrawerOpen}
        onClose={() => setIsBatchDrawerOpen(false)}
        onBatchCreated={() => fetchOrders()}
      />
    </AppLayout>
  );
};

export default OrdersPage;
