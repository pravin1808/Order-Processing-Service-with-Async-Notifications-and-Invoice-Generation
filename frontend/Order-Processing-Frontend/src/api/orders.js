import api from './axios';

/**
 * Order API integration matching OrderController, InvoiceController, and ExportController.
 */

/**
 * Fetch paginated list of orders.
 * ADMIN receives all orders (with optional status filtering).
 * CUSTOMER receives their personal orders.
 *
 * @param {Object} [params] - Optional query params: { page, size, sort, status }
 */
export const getOrders = async (params = {}) => {
  const response = await api.get('/orders', { params });
  return response.data; // Page<OrderSummaryResponse>
};

/**
 * Fetch full order detail by ID including line items and customer info.
 *
 * @param {string} orderId - UUID of the order
 */
export const getOrderById = async (orderId) => {
  const response = await api.get(`/orders/${orderId}`);
  return response.data; // OrderResponse
};

/**
 * Create a new order with customer details and line items.
 *
 * @param {Object} orderData - { customerName, customerEmail, customerPhone, deliveryAddress, items: [{ productName, quantity, unitPrice }] }
 */
export const createOrder = async (orderData) => {
  const response = await api.post('/orders', orderData);
  return response.data; // OrderResponse
};

/**
 * Update order status (ADMIN only).
 *
 * @param {string} orderId - UUID of the order
 * @param {string} status - New OrderStatus: 'CONFIRMED' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED'
 */
export const updateOrderStatus = async (orderId, status) => {
  const response = await api.patch(`/orders/${orderId}/status`, { status });
  return response.data; // OrderResponse
};

/**
 * Download generated PDF invoice for an order.
 * Triggers a browser file download.
 *
 * @param {string} orderId - UUID of the order
 */
export const downloadInvoice = async (orderId) => {
  const response = await api.get(`/orders/${orderId}/invoice`, {
    responseType: 'blob',
  });

  const blob = new Blob([response.data], { type: 'application/pdf' });
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = `invoice-${orderId}.pdf`;
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(url);
};

/**
 * Export all orders as CSV (ADMIN only).
 * Triggers a browser file download.
 */
export const exportOrdersCsv = async () => {
  const response = await api.get('/export/orders/csv', {
    responseType: 'blob',
  });

  const today = new Date().toISOString().slice(0, 10);
  const blob = new Blob([response.data], { type: 'text/csv;charset=utf-8;' });
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = `orders-${today}.csv`;
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(url);
};

/**
 * Fetch daily revenue summary grouped by order status (ADMIN only).
 *
 * @param {string} [date] - Optional date in YYYY-MM-DD format
 */
export const getDailyRevenueSummary = async (date) => {
  const params = date ? { date } : {};
  const response = await api.get('/export/revenue/summary', { params });
  return response.data; // DailyRevenueReportResponse
};

/**
 * Download daily revenue CSV summary grouped by order status (ADMIN only).
 *
 * @param {string} [date] - Optional date in YYYY-MM-DD format
 */
export const downloadDailyRevenueCsv = async (date) => {
  const params = date ? { date } : {};
  const response = await api.get('/export/revenue/csv', {
    params,
    responseType: 'blob',
  });

  const targetDate = date || new Date().toISOString().slice(0, 10);
  const blob = new Blob([response.data], { type: 'text/csv;charset=utf-8;' });
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = `daily-revenue-${targetDate}.csv`;
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(url);
};

/**
 * Manually trigger the nightly revenue CSV export job (ADMIN only).
 *
 * @param {string} [date] - Optional date in YYYY-MM-DD format
 */
export const triggerNightlyRevenueJob = async (date) => {
  const params = date ? { date } : {};
  const response = await api.post('/export/revenue/trigger', null, { params });
  return response.data;
};
