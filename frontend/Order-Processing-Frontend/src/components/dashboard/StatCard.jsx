import React from 'react';

/**
 * Metric stat card for admin dashboard inspired by seller central analytics.
 *
 * @param {string} label - Metric label (e.g. TOTAL ORDERS)
 * @param {string|number} value - Main stat value
 * @param {string} [sub] - Supporting context or trend
 * @param {string} [icon] - Optional icon
 * @param {string} [highlightColor] - Optional accent color
 */
export const StatCard = ({ label, value, sub, icon, highlightColor }) => {
  return (
    <div className="stat-card">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <div className="stat-card-label">{label}</div>
        {icon && <span style={{ fontSize: '18px', opacity: 0.8 }}>{icon}</span>}
      </div>
      <div className="stat-card-value" style={{ color: highlightColor || 'var(--text-primary)' }}>
        {value}
      </div>
      {sub && <div className="stat-card-sub">{sub}</div>}
    </div>
  );
};

export default StatCard;
