import React, { useState, useEffect, useRef } from 'react';
import { getNotifications, markNotificationAsRead, markAllNotificationsAsRead } from '../../api/notifications';

export const NotificationBell = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [unreadCount, setUnreadCount] = useState(0);
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(false);
  const popoverRef = useRef(null);

  const fetchList = async () => {
    try {
      const data = await getNotifications();
      setUnreadCount(data.unreadCount || 0);
      setNotifications(data.notifications || []);
    } catch (err) {
      // Silent error in background polling
      console.warn('Failed to fetch notifications', err);
    }
  };

  useEffect(() => {
    fetchList();
    // Auto-poll every 15 seconds to receive new async notifications in real time
    const interval = setInterval(fetchList, 15000);
    return () => clearInterval(interval);
  }, []);

  // Close dropdown on click outside
  useEffect(() => {
    const handleClickOutside = (e) => {
      if (popoverRef.current && !popoverRef.current.contains(e.target)) {
        setIsOpen(false);
      }
    };
    if (isOpen) {
      document.addEventListener('mousedown', handleClickOutside);
    }
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [isOpen]);

  const handleToggle = () => {
    setIsOpen(!isOpen);
    if (!isOpen) {
      fetchList();
    }
  };

  const handleItemClick = async (item) => {
    if (!item.read) {
      try {
        await markNotificationAsRead(item.id);
        setNotifications((prev) =>
          prev.map((n) => (n.id === item.id ? { ...n, read: true } : n))
        );
        setUnreadCount((prev) => Math.max(0, prev - 1));
      } catch (err) {
        console.error('Failed to mark read', err);
      }
    }
  };

  const handleMarkAllRead = async () => {
    try {
      await markAllNotificationsAsRead();
      setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
      setUnreadCount(0);
    } catch (err) {
      console.error('Failed to mark all read', err);
    }
  };

  const getEventIcon = (type) => {
    switch (type) {
      case 'ORDER_CONFIRMED':
        return { icon: '✅', className: 'confirmed' };
      case 'ORDER_SHIPPED':
        return { icon: '🚚', className: 'shipped' };
      default:
        return { icon: '⚡', className: 'created' };
    }
  };

  const formatRelativeTime = (isoString) => {
    if (!isoString) return '';
    const date = new Date(isoString);
    const now = new Date();
    const diffMs = now - date;
    const diffSec = Math.floor(diffMs / 1000);
    const diffMin = Math.floor(diffSec / 60);
    const diffHours = Math.floor(diffMin / 60);

    if (diffMin < 1) return 'Just now';
    if (diffMin < 60) return `${diffMin}m ago`;
    if (diffHours < 24) return `${diffHours}h ago`;
    return date.toLocaleDateString('en-IN', { month: 'short', day: 'numeric' });
  };

  return (
    <div className="navbar-notification-wrapper" ref={popoverRef}>
      <button
        type="button"
        className="btn-notification-bell"
        onClick={handleToggle}
        aria-label="Notifications"
        title="Async Event Notifications"
      >
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9" />
          <path d="M10.3 21a1.94 1.94 0 0 0 3.4 0" />
        </svg>

        {unreadCount > 0 && (
          <span className="notification-badge-count">
            {unreadCount > 9 ? '9+' : unreadCount}
          </span>
        )}
      </button>

      {isOpen && (
        <div className="notification-popover">
          <div className="notification-header">
            <div className="notification-title-wrap">
              <h3 className="notification-title">Notifications</h3>
              {unreadCount > 0 && (
                <span className="notification-count-tag">{unreadCount} new</span>
              )}
            </div>

            {unreadCount > 0 && (
              <button
                type="button"
                className="notification-mark-all"
                onClick={handleMarkAllRead}
              >
                Mark all read
              </button>
            )}
          </div>

          <div className="notification-list">
            {notifications.length === 0 ? (
              <div className="notification-empty">
                <div style={{ fontSize: '24px', marginBottom: '8px' }}>🔔</div>
                <strong>No notifications yet</strong>
                <p style={{ margin: '4px 0 0 0', fontSize: '12px' }}>
                  You will receive async event updates when orders are placed, confirmed, or shipped.
                </p>
              </div>
            ) : (
              notifications.map((item) => {
                const { icon, className } = getEventIcon(item.eventType);
                return (
                  <div
                    key={item.id}
                    className={`notification-item ${!item.read ? 'unread' : ''}`}
                    onClick={() => handleItemClick(item)}
                  >
                    <div className={`notification-item-icon ${className}`}>
                      {icon}
                    </div>

                    <div className="notification-content">
                      <div className="notification-item-title">
                        <span>{item.title}</span>
                        {!item.read && <span className="notification-dot"></span>}
                      </div>
                      <div className="notification-item-message">{item.message}</div>
                      <div className="notification-item-time">{formatRelativeTime(item.createdAt)}</div>
                    </div>
                  </div>
                );
              })
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default NotificationBell;
