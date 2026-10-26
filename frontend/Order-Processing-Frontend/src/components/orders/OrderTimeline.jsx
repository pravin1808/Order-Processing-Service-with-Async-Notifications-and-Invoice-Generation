import React from 'react';

/**
 * Vertical step tracker inspired by Flipkart / Amazon shipment tracker.
 *
 * @param {string} status - Current status: PENDING | CONFIRMED | SHIPPED | DELIVERED | CANCELLED
 * @param {string} createdAt - Order creation timestamp
 * @param {string} [updatedAt] - Last status update timestamp
 */
export const OrderTimeline = ({ status, createdAt, updatedAt }) => {
  const currentStatus = (status || 'PENDING').toUpperCase();

  const formatDate = (isoString) => {
    if (!isoString) return '';
    try {
      const d = new Date(isoString);
      return d.toLocaleDateString('en-IN', {
        day: '2-digit',
        month: 'short',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    } catch {
      return isoString;
    }
  };

  if (currentStatus === 'CANCELLED') {
    return (
      <div className="timeline">
        <div className="timeline-step done">
          <div className="timeline-dot">✓</div>
          <div className="timeline-content">
            <div className="timeline-label">Order Placed</div>
            <div className="timeline-meta">{formatDate(createdAt)}</div>
          </div>
        </div>
        <div className="timeline-step cancelled">
          <div className="timeline-dot">✕</div>
          <div className="timeline-content">
            <div className="timeline-label" style={{ color: 'var(--danger)' }}>Order Cancelled</div>
            <div className="timeline-meta">{updatedAt ? formatDate(updatedAt) : 'Order has been cancelled'}</div>
          </div>
        </div>
      </div>
    );
  }

  const steps = [
    { key: 'PENDING', label: 'Order Placed', desc: 'Received & awaiting confirmation' },
    { key: 'CONFIRMED', label: 'Order Confirmed', desc: 'Processing in warehouse' },
    { key: 'SHIPPED', label: 'Shipped', desc: 'Handed over to carrier' },
    { key: 'DELIVERED', label: 'Delivered', desc: 'Package delivered to recipient' },
  ];

  const statusOrder = ['PENDING', 'CONFIRMED', 'SHIPPED', 'DELIVERED'];
  const currentIndex = statusOrder.indexOf(currentStatus);

  return (
    <div className="timeline">
      {steps.map((step, idx) => {
        const isDone = idx < currentIndex;
        const isActive = idx === currentIndex;

        let stepClass = '';
        let dotContent = idx + 1;

        if (isDone) {
          stepClass = 'done';
          dotContent = '✓';
        } else if (isActive) {
          stepClass = 'active';
          dotContent = '●';
        }

        return (
          <div key={step.key} className={`timeline-step ${stepClass}`}>
            <div className="timeline-dot">{dotContent}</div>
            <div className="timeline-content">
              <div className="timeline-label" style={{ fontWeight: isActive ? 600 : 500 }}>
                {step.label} {isActive && <span style={{ fontSize: '11px', color: 'var(--brand)', marginLeft: '6px' }}>● Current</span>}
              </div>
              <div className="timeline-meta">
                {isActive && updatedAt && formatDate(updatedAt) ? formatDate(updatedAt) : step.desc}
              </div>
            </div>
          </div>
        );
      })}
    </div>
  );
};

export default OrderTimeline;
