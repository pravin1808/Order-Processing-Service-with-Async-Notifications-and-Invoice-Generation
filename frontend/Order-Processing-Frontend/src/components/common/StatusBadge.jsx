import React from 'react';

/**
 * StatusBadge component rendering a consistent color-coded pill with status dot.
 *
 * @param {string} status - PENDING | CONFIRMED | SHIPPED | DELIVERED | CANCELLED
 * @param {string} [className] - Optional extra CSS class
 */
export const StatusBadge = ({ status, className = '' }) => {
  const normalized = (status || 'PENDING').toUpperCase();

  return (
    <span className={`badge badge-${normalized} ${className}`}>
      {normalized}
    </span>
  );
};

export default StatusBadge;
