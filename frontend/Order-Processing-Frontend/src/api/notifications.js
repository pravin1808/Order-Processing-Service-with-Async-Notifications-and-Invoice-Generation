import api from './axios';

/**
 * Fetch current user notifications and unread counter.
 */
export const getNotifications = async () => {
  const response = await api.get('/notifications');
  return response.data; // { unreadCount: number, notifications: NotificationResponse[] }
};

/**
 * Mark a specific notification as read.
 */
export const markNotificationAsRead = async (id) => {
  const response = await api.patch(`/notifications/${id}/read`);
  return response.data;
};

/**
 * Mark all notifications for the current user as read.
 */
export const markAllNotificationsAsRead = async () => {
  const response = await api.patch('/notifications/read-all');
  return response.data;
};
