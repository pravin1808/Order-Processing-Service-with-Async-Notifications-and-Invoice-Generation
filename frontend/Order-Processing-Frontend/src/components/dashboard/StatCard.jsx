import React from 'react';

/**
 * Modern Metric Stat Card with subtle elevation, icon badge, and hover interaction.
 *
 * @param {string} label - Metric label (e.g. TOTAL ORDERS)
 * @param {string|number} value - Main stat value
 * @param {string} [sub] - Supporting context or trend
 * @param {string} [icon] - Optional emoji/icon
 * @param {string} [highlightColor] - Optional accent color
 */
export const StatCard = ({ label, value, sub, icon, highlightColor }) => {
  return (
    <div
      className="stat-card"
      style={{
        background: '#FFFFFF',
        border: '1px solid #E2E8F0',
        borderRadius: '12px',
        padding: '20px 22px',
        boxShadow: '0 1px 3px rgba(0, 0, 0, 0.04)',
        transition: 'all 0.2s cubic-bezier(0.4, 0, 0.2, 1)',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'space-between',
        cursor: 'default',
      }}
      onMouseEnter={(e) => {
        e.currentTarget.style.transform = 'translateY(-2px)';
        e.currentTarget.style.boxShadow = '0 8px 16px -2px rgba(0, 0, 0, 0.06)';
        e.currentTarget.style.borderColor = highlightColor || '#CBD5E1';
      }}
      onMouseLeave={(e) => {
        e.currentTarget.style.transform = 'translateY(0)';
        e.currentTarget.style.boxShadow = '0 1px 3px rgba(0, 0, 0, 0.04)';
        e.currentTarget.style.borderColor = '#E2E8F0';
      }}
    >
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
        <span
          style={{
            fontSize: '11px',
            fontWeight: 700,
            letterSpacing: '0.05em',
            textTransform: 'uppercase',
            color: '#64748B',
          }}
        >
          {label}
        </span>
        {icon && (
          <div
            style={{
              width: '32px',
              height: '32px',
              borderRadius: '8px',
              background: highlightColor ? `${highlightColor}15` : '#EFF6FF',
              color: highlightColor || '#2563EB',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: '15px',
            }}
          >
            {icon}
          </div>
        )}
      </div>

      <div>
        <div
          style={{
            fontSize: '28px',
            fontWeight: 800,
            letterSpacing: '-0.03em',
            color: highlightColor || '#0F172A',
            lineHeight: 1.1,
          }}
        >
          {value}
        </div>
        {sub && (
          <div style={{ fontSize: '12px', color: '#64748B', marginTop: '6px' }}>
            {sub}
          </div>
        )}
      </div>
    </div>
  );
};

export default StatCard;
